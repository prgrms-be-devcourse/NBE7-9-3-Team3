package org.example.backend.global.websocketconfig;

import lombok.RequiredArgsConstructor;
import org.example.backend.domain.member.entity.Member;
import org.example.backend.domain.member.repository.MemberRepository;
import org.example.backend.domain.member.service.AuthTokenService;
import org.example.backend.global.security.CustomUserDetails;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class StompHandler implements ChannelInterceptor {

    private final AuthTokenService authTokenService;
    private final MemberRepository memberRepository;

    /*
    STOMP 메세지 전송 전 JWT 인증을 처리
        - 웹소켓 연결 시 헤더에서 JWT 토큰 추출
        - 이후 세션에 memberId 저장
     */
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) return message;

        // CONNECT 단계에서 토큰 검증
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = accessor.getFirstNativeHeader("Authorization");

            // Authorization 헤더 검증
            if (token == null || !token.startsWith("Bearer ")) {
                throw new IllegalArgumentException("JWT 토큰이 누락되었습니다.");
            }

            token = token.substring(7);

            // JWT Payload 검증
            Map<String, Object> payload = authTokenService.payloadOrNull(token);
            if (payload == null) {
                throw new IllegalArgumentException("JWT 토큰이 유효하지 않습니다.");
            }

            // 사용자 정보 조회
            Long memberId = ((Number) payload.get("id")).longValue();
            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

            // 인증 객체를 SecurityContext에 저장
            CustomUserDetails userDetails = new CustomUserDetails(member);
            Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);

            accessor.setUser(auth);

            // STOMP 세션에 memberId 저장 (이후 메시지 전송 시 사용)
            accessor.getSessionAttributes().put("memberId", memberId);
        }

        return message;
    }
}
