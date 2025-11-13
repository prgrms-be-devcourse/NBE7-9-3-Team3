package org.example.backend.domain.follow.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.backend.domain.follow.dto.*;
import org.example.backend.domain.follow.service.FollowService;
import org.example.backend.global.requestcontext.RequestContext;
import org.example.backend.global.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/follows")
@RequiredArgsConstructor
@Tag(name = "Follow", description = "팔로우 관리 API")
public class FollowController {

    private final FollowService followService;
    private final RequestContext requestContext;

    @Operation(summary = "팔로우하기", description = "특정 사용자를 팔로우합니다.")
    @PostMapping("/{followeeId}")
    public ResponseEntity<ApiResponse<FollowResponseDto>> follow(
        @Parameter(description = "팔로우할 사용자 ID", required = true)
        @PathVariable Long followeeId) {
        Long currentMemberId = requestContext.getCurrentMemberId();
        ApiResponse<FollowResponseDto> response = followService.follow(currentMemberId, followeeId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "언팔로우하기", description = "특정 사용자의 팔로우를 취소합니다.")
    @DeleteMapping("/{followeeId}")
    public ResponseEntity<ApiResponse<Void>> unfollow(
        @Parameter(description = "언팔로우할 사용자 ID", required = true)
        @PathVariable Long followeeId) {
        Long currentMemberId = requestContext.getCurrentMemberId();
        ApiResponse<Void> response = followService.unfollow(currentMemberId, followeeId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "팔로워 목록 조회", description = "특정 사용자의 팔로워 목록을 조회합니다.")
    @GetMapping("/{memberId}/followers")
    public ResponseEntity<ApiResponse<FollowListResponseDto>> getFollowers(
        @Parameter(description = "팔로워 목록을 조회할 사용자 ID", required = true)
        @PathVariable Long memberId) {
        ApiResponse<FollowListResponseDto> response = followService.getFollowers(memberId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "팔로잉 목록 조회", description = "특정 사용자의 팔로잉 목록을 조회합니다.")
    @GetMapping("/{memberId}/followings")
    public ResponseEntity<ApiResponse<FollowListResponseDto>> getFollowings(
        @Parameter(description = "팔로잉 목록을 조회할 사용자 ID", required = true)
        @PathVariable Long memberId) {
        ApiResponse<FollowListResponseDto> response = followService.getFollowings(memberId);
        return ResponseEntity.ok(response);
    }

}
