package org.example.backend.domain.post.controller

import org.example.backend.domain.post.dto.*
import org.example.backend.domain.post.entity.Post
import org.example.backend.domain.post.service.PostService
import org.example.backend.global.response.ApiResponse
import org.example.backend.global.security.CustomUserDetails
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/posts")
class PostController (
    private val postService: PostService
) : PostControllerSpec{


    @GetMapping("/my")
    override fun getMyPosts(
        @RequestParam boardType: Post.BoardType,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ApiResponse<List<MyPostReadResponseDto>> {

        val response = postService.getMyPosts(boardType, userDetails.id!!)

        return ApiResponse.ok("내가 쓴 게시글 다건 조회", response)
    }

    @GetMapping
    override fun getPosts(
        @RequestParam boardType: Post.BoardType,
        @RequestParam(defaultValue = "ALL") filterType: FilterType,  // "ALL" or "FOLLOWING"
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @RequestParam(required = false) keyword: String?,
        @RequestParam(defaultValue = "ALL") category: Post.Category,
        @PageableDefault(
            size = 10,
            sort = ["id"],
            direction = Sort.Direction.DESC
        ) pageable: Pageable
    ): ApiResponse<PostListResponseDto> {

        val response = postService.getPosts(
            boardType, filterType, userDetails.member, keyword, category, pageable
        )

        return ApiResponse.ok("게시글 다건 조회", response)
    }

    @GetMapping("/{id}")
    override fun getPost(
        @PathVariable id: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ApiResponse<PostReadResponseDto> {
        val response = postService.getPostById(id, userDetails.member)


        return ApiResponse.ok("${id}번 게시글 단건 조회", response)
    }

    @DeleteMapping("/{id}")
    override fun deletePost(
        @PathVariable id: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ApiResponse<Void> {
        postService.delete(id, userDetails.member)

        return ApiResponse.ok("${id}번 게시글 삭제")
    }

    @PostMapping
    override fun createPost(
        @RequestBody reqBody: PostWriteRequestDto,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ApiResponse<PostResponseDto> {
        val response = postService.write(reqBody, userDetails.member)

        return ApiResponse.ok("게시글 생성", response)
    }

    @PatchMapping("/{id}")
    override fun modifyPost(
        @PathVariable id: Long,
        @RequestBody reqBody: PostModifyRequestDto,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ApiResponse<PostResponseDto> {
        val response = postService.modify(id, reqBody, userDetails.member)

        return ApiResponse.ok("${id}번 게시글 수정", response)
    }
}
