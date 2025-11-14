package org.example.backend.global.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.example.backend.domain.member.entity.Member;
import org.example.backend.domain.member.repository.MemberRepository;
import org.example.backend.domain.member.service.AuthTokenService;
import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;
import org.example.backend.global.requestcontext.RequestContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;


@Component
@RequiredArgsConstructor
public class CustomAuthenticationFilter extends OncePerRequestFilter {

    private final AuthTokenService authTokenService;
    private final MemberRepository memberRepository;
    private final RequestContext rq;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        logger.debug("CustomAuthenticationFilter called");

        try {
            authenticate(request, response, filterChain);
        } catch (BusinessException e) {
            ErrorCode errorCode = e.getErrorCode();
            response.setContentType("application/json; charset=UTF-8");
            response.setStatus(errorCode.getStatus().value());
            response.getWriter().write("""
                    {
                        "resultCode": "%s",
                        "msg": "%s"
                    }
                    """.formatted(errorCode.getCode(), errorCode.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error in authentication filter", e);
            throw e;
        }
    }

    private void authenticate(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        
        // API 경로가 아닌 경우 통과 (Swagger UI, H2 Console, WebSocket 등)
        if(!requestURI.startsWith("/api/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // WebSocket 경로 제외
        if(requestURI.startsWith("/ws")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Swagger UI 관련 경로들 제외
        if(requestURI.startsWith("/swagger-ui") || 
           requestURI.startsWith("/api-docs") || 
           requestURI.startsWith("/webjars") ||
           requestURI.startsWith("/v3/api-docs")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 허용된 API 경로들
        if(List.of("/api/members/join", "/api/members/login").contains(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken;
        String headerAuthorization = rq.getHeader("Authorization", "");

        if (!headerAuthorization.isBlank()) {
            if (!headerAuthorization.startsWith("Bearer ")) {
                throw new BusinessException(ErrorCode.UNAUTHORIZED_ACCESS);
            }
            accessToken = headerAuthorization.substring(7);
        } else {
            accessToken = rq.getCookieValue("accessToken", "");
        }

        if (accessToken == null || accessToken.isBlank()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        Map<String, Object> payload = authTokenService.payloadOrNull(accessToken);

        if (payload == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        long id = (long) payload.get("id");

        // 데이터베이스에서 실제 Member 조회
        Optional<Member> optionalMember = memberRepository.findById(id);
        if (optionalMember.isEmpty()) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }

        Member member = optionalMember.get();

        UserDetails user = new CustomUserDetails(member);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
            user,
            user.getPassword(),
            user.getAuthorities()
        );

        SecurityContextHolder
            .getContext()
            .setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}