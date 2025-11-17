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
        // JWT 토큰을 HttpOnly 쿠키로 설정
        result.data?.let {
            // 로그인한 사용자 정보로 토큰 생성
            val member = memberService.findByEmail(request.email)
                ?: throw BusinessException(ErrorCode.MEMBER_NOT_FOUND)
            val accessToken = memberService.generateAccessToken(member)
            requestContext.setCookie("accessToken", accessToken)
        }
        return result
    }

    @PostMapping("/logout")
    override fun logout(): ApiResponse<Void> {
        requestContext.deleteCookie("accessToken")
        return ApiResponse.ok("로그아웃에 성공했습니다.")
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
