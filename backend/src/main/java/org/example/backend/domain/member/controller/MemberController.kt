package org.example.backend.domain.member.controller

import jakarta.validation.Valid
import org.example.backend.domain.member.dto.*
import org.example.backend.domain.member.service.MemberService
import org.example.backend.global.exception.BusinessException
import org.example.backend.global.exception.ErrorCode
import org.example.backend.global.requestcontext.RequestContext
import org.example.backend.global.response.ApiResponse
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/members")
class MemberController(
    private val memberService: MemberService,
    private val requestContext: RequestContext
) : MemberControllerSpec {

    @PostMapping("/join")
    override fun join(request: MemberJoinRequestDto): ApiResponse<MemberJoinResponseDto> {
        return memberService.join(request, request.profileImageUrl)
    }

    @PostMapping("/login")
    override fun login(
        @RequestBody request: @Valid MemberLoginRequestDto
    ): ApiResponse<MemberLoginResponseDto> {
        val result = memberService.login(request)
        // 액세스 토큰과 리프레시 토큰을 쿠키로 설정
        result.data?.let {
            // 로그인한 사용자 정보로 토큰 생성
            val member = memberService.findByEmail(request.email)
                ?: throw BusinessException(ErrorCode.MEMBER_NOT_FOUND)
            val accessToken = memberService.generateAccessToken(member)
            val refreshToken = memberService.generateRefreshToken(member)

            // 액세스 토큰과 리프레시 토큰을 쿠키로 설정 (HttpOnly)
            requestContext.setCookie("accessToken", accessToken)
            requestContext.setCookie("refreshToken", refreshToken)
        }
        val responseWithoutRefreshToken = result.data?.let {
            MemberLoginResponseDto(
                memberId = it.memberId,
                email = it.email,
                nickname = it.nickname,
                profileImage = it.profileImage,
            )
        }
        return ApiResponse(result.resultCode, result.msg, responseWithoutRefreshToken)
    }

    @PostMapping("/logout")
    override fun logout(): ApiResponse<Void> {
        // 리프레시 토큰 가져오기 (쿠키에서)
        val refreshToken = requestContext.getCookieValue("refreshToken", "")

        // 리프레시 토큰이 있으면 Redis에서 삭제
        refreshToken?.takeIf { it.isNotEmpty() }?.let {
            memberService.deleteRefreshToken(it)
        }

        // 액세스 토큰과 리프레시 토큰 쿠키 삭제
        requestContext.deleteCookie("accessToken")
        requestContext.deleteCookie("refreshToken")
        
        return ApiResponse.ok("로그아웃에 성공했습니다.")
    }

    @PostMapping("/refresh")
    fun refreshToken(): ApiResponse<Void> {
        // 리프레시 토큰을 쿠키에서 가져오기 (HttpOnly 쿠키이므로 JavaScript 접근 불가)
        val refreshToken = requestContext.getCookieValue("refreshToken", "")
            ?: throw BusinessException(ErrorCode.REFRESH_TOKEN_INVALID)

        // 트랜잭션으로 토큰 갱신 처리
        val (newAccessToken, newRefreshToken) = memberService.refreshToken(refreshToken)

        // 새로운 토큰들을 쿠키로 설정
        requestContext.setCookie("accessToken", newAccessToken)
        requestContext.setCookie("refreshToken", newRefreshToken)

        return ApiResponse.ok("토큰이 갱신되었습니다.")
    }

    @PutMapping("/me")
    override fun edit(
        @RequestBody request: @Valid MemberEditRequestDto
    ): ApiResponse<MemberEditResponseDto> {
        val result = memberService.edit(request, request.profileImageUrl)

        // 새로운 토큰이 있는 경우 쿠키 업데이트
        result.data?.newAccessToken?.let {
            requestContext.setCookie("accessToken", it)
        }

        return result
    }

    @GetMapping("/me")
    override fun myPage(): ApiResponse<MemberResponseDto> {
        return memberService.myPage()
    }

    @PutMapping("/me/profile-image")
    override fun updateProfileImage(
        @RequestBody request: Map<String, String>
    ): ApiResponse<MemberEditResponseDto> {
        val result = memberService.updateProfileImage(request["profileImageUrl"])

        // 새로운 토큰이 있는 경우 쿠키 업데이트
        result.data?.newAccessToken?.let {
            requestContext.setCookie("accessToken", it)
        }

        return result
    }

    @GetMapping("/search")
    override fun searchMembers(
        @RequestParam nickname: String?
    ): ApiResponse<MemberSearchListResponseDto> {
        return memberService.searchMembers(nickname)
    }
}
