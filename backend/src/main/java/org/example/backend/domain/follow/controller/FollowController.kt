package org.example.backend.domain.follow.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.example.backend.domain.follow.dto.FollowListResponseDto
import org.example.backend.domain.follow.dto.FollowResponseDto
import org.example.backend.domain.follow.service.FollowService
import org.example.backend.global.exception.BusinessException
import org.example.backend.global.exception.ErrorCode
import org.example.backend.global.requestcontext.RequestContext
import org.example.backend.global.response.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/follows")
@Tag(name = "Follow", description = "팔로우 관리 API")
class FollowController(
    private val followService: FollowService,
    private val requestContext: RequestContext
) {

    @Operation(summary = "팔로우하기", description = "특정 사용자를 팔로우합니다.")
    @PostMapping("/{followeeId}")
    fun follow(
        @Parameter(description = "팔로우할 사용자 ID", required = true) @PathVariable followeeId: Long?
    ): ResponseEntity<ApiResponse<FollowResponseDto>> {
        val currentMemberId = requestContext.currentMemberId
            ?: throw BusinessException(ErrorCode.MEMBER_NOT_FOUND)

        val response = followService.follow(currentMemberId, followeeId)
        return ResponseEntity.ok(response)
    }

    @Operation(summary = "언팔로우하기", description = "특정 사용자의 팔로우를 취소합니다.")
    @DeleteMapping("/{followeeId}")
    fun unfollow(
        @Parameter(description = "언팔로우할 사용자 ID", required = true) @PathVariable followeeId: Long?
    ): ResponseEntity<ApiResponse<Void>> {
        val currentMemberId = requestContext.currentMemberId
        val response = followService.unfollow(currentMemberId, followeeId)
        return ResponseEntity.ok(response)
    }

    @Operation(summary = "팔로워 목록 조회", description = "특정 사용자의 팔로워 목록을 조회합니다.")
    @GetMapping("/{memberId}/followers")
    fun getFollowers(
        @Parameter(
            description = "팔로워 목록을 조회할 사용자 ID",
            required = true
        ) @PathVariable memberId: Long?
    ): ResponseEntity<ApiResponse<FollowListResponseDto>> {
        val response = followService.getFollowers(memberId)
        return ResponseEntity.ok(response)
    }

    @Operation(summary = "팔로잉 목록 조회", description = "특정 사용자의 팔로잉 목록을 조회합니다.")
    @GetMapping("/{memberId}/followings")
    fun getFollowings(
        @Parameter(
            description = "팔로잉 목록을 조회할 사용자 ID",
            required = true
        ) @PathVariable memberId: Long?
    ): ResponseEntity<ApiResponse<FollowListResponseDto>> {
        val response = followService.getFollowings(memberId)
        return ResponseEntity.ok(response)
    }
}
