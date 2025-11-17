package org.example.backend.domain.post.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.example.backend.domain.post.dto.*
import org.example.backend.domain.post.entity.Post
import org.example.backend.global.response.ApiResponse
import org.example.backend.global.security.CustomUserDetails
import org.springframework.data.domain.Pageable
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam

@Tag(name = "Post", description = "질문/자랑 게시판 관리 API")
interface PostControllerSpec {

    @Operation(summary = "내 게시글 조회", description = "사용자가 작성한 게시글 목록을 조회합니다.")
    fun getMyPosts(
        @RequestParam boardType: Post.BoardType,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ApiResponse<List<MyPostReadResponseDto>>

    @Operation(summary = "게시글 목록 조회", description = "게시판의 게시글 목록을 조회합니다.")
    fun getPosts(
        @RequestParam boardType: Post.BoardType,
        @RequestParam(defaultValue = "ALL") filterType: FilterType,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @RequestParam(required = false) keyword: String?,
        @RequestParam(defaultValue = "ALL") category: Post.Category,
        pageable: Pageable
    ): ApiResponse<PostListResponseDto>

    @Operation(summary = "게시글 조회", description = "특정 게시글의 상세 정보를 조회합니다.")
    fun getPost(
        @PathVariable id: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ApiResponse<PostReadResponseDto>

    @Operation(summary = "게시글 삭제", description = "게시글을 삭제합니다.")
    fun deletePost(
        @PathVariable id: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ApiResponse<Void>

    @Operation(summary = "게시글 생성", description = "새로운 게시글을 생성합니다.")
    fun createPost(
        @RequestBody reqBody: PostWriteRequestDto,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ApiResponse<PostResponseDto>

    @Operation(summary = "게시글 수정", description = "기존 게시글을 수정합니다.")
    fun modifyPost(
        @PathVariable id: Long,
        @RequestBody reqBody: PostModifyRequestDto,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ApiResponse<PostResponseDto>
}
