package org.example.backend.global.websocketconfig

import org.example.backend.domain.member.repository.MemberRepository
import org.example.backend.domain.member.service.AuthTokenService
import org.example.backend.global.security.CustomUserDetails
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.MessageHeaderAccessor
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component
class StompHandler(
    private val authTokenService: AuthTokenService,
    private val memberRepository: MemberRepository,
) : ChannelInterceptor {

    /*
    STOMP 메세지 전송 전 JWT 인증을 처리
        - 웹소켓 연결 시 헤더에서 JWT 토큰 추출
        - 이후 세션에 memberId 저장
     */
    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*> {
        val accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor::class.java)
            ?: return message

        // CONNECT 단계에서 토큰 검증
        if (accessor.command == StompCommand.CONNECT) {
            var token = accessor.getFirstNativeHeader("Authorization")

            // Authorization 헤더 검증
            require(!(token == null || !token.startsWith("Bearer "))) {
                "JWT 토큰이 누락되었습니다."
            }

            token = token.substring(7)

            // JWT Payload 검증
            val payload = authTokenService.payloadOrNull(token)
            requireNotNull(payload) { "JWT 토큰이 유효하지 않습니다." }

            // 사용자 정보 조회
            val memberId = (payload["id"] as Number).toLong()
            val member = memberRepository.findById(memberId)
                .orElseThrow { IllegalArgumentException("존재하지 않는 회원입니다.") }

            // 인증 객체를 SecurityContext에 저장
            val userDetails = CustomUserDetails(member)
            val auth: Authentication =
                UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)
            SecurityContextHolder.getContext().authentication = auth

            accessor.user = auth

            // STOMP 세션에 memberId 저장 (이후 메시지 전송 시 사용)
            accessor.sessionAttributes?.put("memberId", memberId)
        }

        return message
    }
}
