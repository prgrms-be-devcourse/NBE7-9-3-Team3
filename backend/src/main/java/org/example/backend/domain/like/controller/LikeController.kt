package org.example.backend.domain.like.controller

import io.swagger.v3.oas.annotations.tags.Tag
import org.example.backend.domain.like.dto.PostLikeResponseDto
import org.example.backend.domain.like.service.LikeService
import org.example.backend.global.response.ApiResponse
import org.example.backend.global.security.CustomUserDetails
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/posts")
@Tag(name = "Like", description = "자랑게시판 좋아요 관리 API")
class LikeController(
    private val likeService: LikeService
) {

    @PostMapping("/{postId}/likes")
    fun toggleLike(
        @PathVariable postId: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ApiResponse<Map<String, Any>> {
        val response = likeService.toggleLike(postId, userDetails.id!!)
        return ApiResponse.ok("좋아요 토글 완료", response)
    }

    @GetMapping("/likes/my")
    fun getLikedPosts(
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ApiResponse<List<PostLikeResponseDto>> {
        val likedPosts = likeService.getLikedPosts(userDetails.id!!)
        return ApiResponse.ok("좋아요한 글 조회 성공", likedPosts)
    }
}
