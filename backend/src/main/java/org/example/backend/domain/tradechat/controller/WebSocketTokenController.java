package org.example.backend.domain.tradechat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.backend.domain.member.entity.Member;
import org.example.backend.domain.member.repository.MemberRepository;
import org.example.backend.domain.member.service.AuthTokenService;
import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;
import org.example.backend.global.response.ApiResponse;
import org.example.backend.global.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
@Tag(name = "WebSocket Token", description = "웹소켓 전용 JWT 발급 API")
public class WebSocketTokenController {

    private final MemberRepository memberRepository;
    private final AuthTokenService authTokenService;

    // 웹소켓 연결용 10분 수명 임시 토큰 발급
    @Operation(summary = "웹소켓용 단기 Access Token 발급", description = "현재 로그인한 회원의 정보를 기반으로 STOMP 연결 시 사용할 짧은 JWT를 발급합니다.")
    @GetMapping("/token")
    public ApiResponse<String> getWebSocketToken(@AuthenticationPrincipal CustomUserDetails userDetails) {

        // 현재 로그인한 사용자 정보 조회
        Member member = memberRepository.findById(userDetails.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        // 단기 JWT 생성
        String token = authTokenService.genTempToken(member);
        return ApiResponse.ok("웹소켓 토큰이 발급되었습니다.", token);
    }
}
