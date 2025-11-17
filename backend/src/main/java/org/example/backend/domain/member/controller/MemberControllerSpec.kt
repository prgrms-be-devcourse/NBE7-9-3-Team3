package org.example.backend.domain.member.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.example.backend.domain.member.dto.*
import org.example.backend.global.response.ApiResponse
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam

@Tag(name = "Member", description = "회원 관리 API")
interface MemberControllerSpec {
    @Operation(summary = "회원가입", description = "새로운 회원을 등록합니다.")
    fun join(
        @Parameter(
            description = "회원가입 정보",
            required = true
        ) @RequestBody request: MemberJoinRequestDto
    ): ApiResponse<MemberJoinResponseDto>

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인합니다.")
    fun login(
        @Parameter(
            description = "로그인 정보",
            required = true
        ) @RequestBody request: MemberLoginRequestDto
    ): ApiResponse<MemberLoginResponseDto>

    @Operation(summary = "로그아웃", description = "현재 로그인된 사용자를 로그아웃합니다.")
    fun logout(): ApiResponse<Void>

    @Operation(summary = "회원정보 수정", description = "현재 로그인된 사용자의 정보를 수정합니다.")
    fun edit(
        @Parameter(
            description = "회원정보 수정 데이터",
            required = true
        ) @RequestBody request: MemberEditRequestDto
    ): ApiResponse<MemberEditResponseDto>

    @Operation(summary = "내 정보 조회", description = "현재 로그인된 사용자의 정보를 조회합니다.")
    fun myPage(): ApiResponse<MemberResponseDto>

    @Operation(summary = "프로필 이미지 수정", description = "현재 로그인된 사용자의 프로필 이미지를 수정합니다.")
    fun updateProfileImage(
        @Parameter(
            description = "프로필 이미지 URL",
            required = true
        ) @RequestBody request: Map<String, String>
    ): ApiResponse<MemberEditResponseDto>

    @Operation(summary = "회원 검색", description = "닉네임으로 회원을 검색합니다.")
    fun searchMembers(
        @Parameter(description = "검색할 닉네임", required = true) @RequestParam nickname: String?
    ): ApiResponse<MemberSearchListResponseDto>
}
