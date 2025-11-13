package org.example.backend.domain.member.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.backend.domain.member.dto.*;
import org.example.backend.domain.member.service.MemberService;
import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;
import org.example.backend.global.requestcontext.RequestContext;
import org.example.backend.global.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController implements MemberControllerSpec {

    private final MemberService memberService;
    private final RequestContext requestContext;

    @Override
    @PostMapping("/join")
    public ApiResponse<MemberJoinResponseDto> join(
        @Valid @RequestBody MemberJoinRequestDto request) {
        return memberService.join(request, request.profileImageUrl());
    }

    @Override
    @PostMapping("/login")
    public ApiResponse<MemberLoginResponseDto> login(
        @Valid @RequestBody MemberLoginRequestDto request) {
        ApiResponse<MemberLoginResponseDto> result = memberService.login(request);
        // JWT 토큰을 HttpOnly 쿠키로 설정
        if (result.getData() != null) {
            // 로그인한 사용자 정보로 토큰 생성
            String accessToken = memberService.generateAccessToken(
                memberService.findByEmail(request.email()).orElseThrow(
                    ()->new BusinessException(ErrorCode.MEMBER_NOT_FOUND)));
            requestContext.setCookie("accessToken", accessToken);
        }
        return result;
    }
    @Override
    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        requestContext.deleteCookie("accessToken");
        return ApiResponse.ok("로그아웃에 성공했습니다.");
    }

    @Override
    @PutMapping("/me")
    public ApiResponse<MemberEditResponseDto> edit(
        @Valid @RequestBody MemberEditRequestDto request) {
        ApiResponse<MemberEditResponseDto> result = memberService.edit(request, request.profileImageUrl());

        // 새로운 토큰이 있는 경우 쿠키 업데이트
        if (result.getData() != null && result.getData().newAccessToken() != null) {
            requestContext.setCookie("accessToken", result.getData().newAccessToken());
        }

        return result;
    }

    @Override
    @GetMapping("/me")
    public ApiResponse<MemberResponseDto> myPage(){
        return memberService.myPage();
    }

    @Override
    @PutMapping("/me/profile-image")
    public ApiResponse<MemberEditResponseDto> updateProfileImage(
        @RequestBody java.util.Map<String, String> request) {
        ApiResponse<MemberEditResponseDto> result = memberService.updateProfileImage(request.get("profileImageUrl"));

        // 새로운 토큰이 있는 경우 쿠키 업데이트
        if (result.getData() != null && result.getData().newAccessToken() != null) {
            requestContext.setCookie("accessToken", result.getData().newAccessToken());
        }

        return result;
    }

    @Override
    @GetMapping("/search")
    public ApiResponse<MemberSearchListResponseDto> searchMembers(
        @RequestParam String nickname) {
        return memberService.searchMembers(nickname);
    }

}
