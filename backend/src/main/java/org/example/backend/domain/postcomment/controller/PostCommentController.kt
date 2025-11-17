package org.example.backend.domain.postcomment.controller

import lombok.RequiredArgsConstructor
import org.example.backend.domain.postcomment.dto.*
import org.example.backend.domain.postcomment.service.PostCommentService
import org.example.backend.global.response.ApiResponse
import org.example.backend.global.security.CustomUserDetails
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts/comments")
class PostCommentController (
    private val postCommentService: PostCommentService
) : PostCommentControllerSpec{

    @GetMapping
    override fun getPostComments(
        @RequestParam postId: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ApiResponse<List<PostCommentReadResponseDto>> {
        val response = postCommentService.getPostComments(postId, userDetails.member)

        return ApiResponse.ok("댓글 목록 조회", response)
    }

    @GetMapping("/my")
    override fun getMyPostComments(
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ApiResponse<List<MyPostCommentReadResponseDto>> {
        val response = postCommentService.findMyComments(userDetails.member)
        return ApiResponse.ok("내가 쓴 댓글 목록 조회", response)
    }

    @DeleteMapping("/{commentId}")
    override fun deletePostComment(
        @PathVariable commentId: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ApiResponse<Void> {

        postCommentService.deletePostComment(commentId, userDetails.member)

        return ApiResponse.ok("${commentId}번 댓글 삭제")
    }

    @PostMapping
    override fun createPostComment(
        @RequestBody reqBody: PostCommentCreateRequestDto,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ApiResponse<PostCommentResponseDto> {

        val response = postCommentService.createPostComment(reqBody, userDetails.member)

        return ApiResponse.ok("댓글이 생성되었습니다", response)
    }

    @PatchMapping("/{commentId}")
    override fun modifyPostComment(
        @PathVariable commentId: Long,
        @RequestBody reqBody: PostCommentModifyRequestDto,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ApiResponse<PostCommentResponseDto> {

        val response = postCommentService.modifyPostComment(commentId, reqBody, userDetails.member)

        return ApiResponse.ok("${commentId}번 댓글 수정",response)
    }
}
