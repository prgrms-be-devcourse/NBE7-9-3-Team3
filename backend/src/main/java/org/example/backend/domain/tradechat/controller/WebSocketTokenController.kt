package org.example.backend.domain.tradechat.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.example.backend.domain.member.repository.MemberRepository
import org.example.backend.domain.member.service.AuthTokenService
import org.example.backend.global.exception.BusinessException
import org.example.backend.global.exception.ErrorCode
import org.example.backend.global.response.ApiResponse
import org.example.backend.global.response.ApiResponse.Companion.ok
import org.example.backend.global.security.CustomUserDetails
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/chat")
@Tag(name = "WebSocket Token", description = "웹소켓 전용 JWT 발급 API")
class WebSocketTokenController(
    private val memberRepository: MemberRepository,
    private val authTokenService: AuthTokenService,
) {

    // 웹소켓 연결용 10분 수명 임시 토큰 발급
    @Operation(
        summary = "웹소켓용 단기 Access Token 발급",
        description = "현재 로그인한 회원의 정보를 기반으로 STOMP 연결 시 사용할 짧은 JWT를 발급합니다.",
    )
    @GetMapping("/token")
    fun getWebSocketToken(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ApiResponse<String> {
        val memberId = userDetails.id ?: throw BusinessException(ErrorCode.MEMBER_NOT_FOUND)
        val member = memberRepository.findById(memberId).orElseThrow { BusinessException(ErrorCode.MEMBER_NOT_FOUND) }
        val token = authTokenService.genTempToken(member)

        return ok("웹소켓 토큰이 발급되었습니다.", token)
    }
}
