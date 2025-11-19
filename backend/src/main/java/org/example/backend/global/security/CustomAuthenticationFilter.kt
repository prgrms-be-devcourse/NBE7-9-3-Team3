package org.example.backend.global.security

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.example.backend.domain.member.repository.MemberRepository
import org.example.backend.domain.member.service.AuthTokenService
import org.example.backend.global.exception.BusinessException
import org.example.backend.global.exception.ErrorCode
import org.example.backend.global.requestcontext.RequestContext
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException

@Component
class CustomAuthenticationFilter(
    private val authTokenService: AuthTokenService,
    private val memberRepository: MemberRepository,
    private val rq: RequestContext
) : OncePerRequestFilter() {

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        logger.debug("CustomAuthenticationFilter called")

        try {
            authenticate(request, response, filterChain)
        } catch (e: BusinessException) {
            val errorCode = e.errorCode
            response.contentType = "application/json; charset=UTF-8"
            response.status = errorCode.status.value()
            response.writer.write(
                """
                    {
                        "resultCode": "%s",
                        "msg": "%s"
                    }
                    
                    """.trimIndent().format(errorCode.code, errorCode.message)
            )
        } catch (e: Exception) {
            logger.error("Unexpected error in authentication filter", e)
            throw e
        }
    }

    @Throws(ServletException::class, IOException::class)
    private fun authenticate(
        request: HttpServletRequest,
        response: HttpServletResponse?,
        filterChain: FilterChain
    ) {
        val requestURI = request.requestURI

        // API 경로가 아닌 경우 통과 (Swagger UI, H2 Console, WebSocket 등)
        if (!requestURI.startsWith("/api/")) {
            filterChain.doFilter(request, response)
            return
        }

        // WebSocket 경로 제외
        if (requestURI.startsWith("/ws")) {
            filterChain.doFilter(request, response)
            return
        }

        // Swagger UI 관련 경로들 제외
        if (requestURI.startsWith("/swagger-ui") ||
            requestURI.startsWith("/api-docs") ||
            requestURI.startsWith("/webjars") ||
            requestURI.startsWith("/v3/api-docs")
        ) {
            filterChain.doFilter(request, response)
            return
        }

        // 허용된 API 경로들
        if (listOf(
                "/api/members/join",
                "/api/members/login",
                "/api/members/refresh"
            ).contains(requestURI)
        ) {
            filterChain.doFilter(request, response)
            return
        }

        val accessToken: String?
        val headerAuthorization = rq.getHeader("Authorization", "")

        if (headerAuthorization != null && headerAuthorization.isNotBlank()) {
            if (!headerAuthorization.startsWith("Bearer ")) {
                throw BusinessException(ErrorCode.UNAUTHORIZED_ACCESS)
            }
            accessToken = headerAuthorization.substring(7)
        } else {
            accessToken = rq.getCookieValue("accessToken", "")
        }

        if (accessToken == null || accessToken.isBlank()) {
            throw BusinessException(ErrorCode.UNAUTHORIZED_ACCESS)
        }

        val payload = authTokenService.payloadOrNull(accessToken)

        if (payload == null) {
            throw BusinessException(ErrorCode.UNAUTHORIZED_ACCESS)
        }

        val id = payload["id"] as? Long ?: throw BusinessException(ErrorCode.UNAUTHORIZED_ACCESS)

        // 데이터베이스에서 실제 Member 조회
        val member = memberRepository.findById(id)
            .orElseThrow { BusinessException(ErrorCode.MEMBER_NOT_FOUND) }

        val user: UserDetails = CustomUserDetails(member)

        val authentication: Authentication = UsernamePasswordAuthenticationToken(
            user,
            user.password,
            user.authorities
        )

        SecurityContextHolder
            .getContext()
            .authentication = authentication

        filterChain.doFilter(request, response)
    }
}
