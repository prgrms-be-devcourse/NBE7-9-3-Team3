package org.example.backend.domain.post.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.backend.domain.post.dto.FilterType;
import org.example.backend.domain.post.dto.MyPostReadResponseDto;
import org.example.backend.domain.post.dto.PostListResponseDto;
import org.example.backend.domain.post.dto.PostModifyRequestDto;
import org.example.backend.domain.post.dto.PostReadResponseDto;
import org.example.backend.domain.post.dto.PostWriteRequestDto;
import org.example.backend.domain.post.entity.Post.BoardType;
import org.example.backend.domain.post.entity.Post.Category;
import org.example.backend.domain.post.service.PostService;
import org.example.backend.global.response.ApiResponse;
import org.example.backend.global.security.CustomUserDetails;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostController implements PostControllerSpec {

    private final PostService postService;

    @Override
    @GetMapping("/my")
    public ApiResponse<List<MyPostReadResponseDto>> getMyPosts(
        @RequestParam BoardType boardType,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        List<MyPostReadResponseDto> response = postService.getMyPosts(boardType, userDetails.getId());

        return ApiResponse.ok("내가 쓴 게시글 다건 조회", response);
    }

    @Override
    @GetMapping
    public ApiResponse<PostListResponseDto> getPosts(
        @RequestParam BoardType boardType,
        @RequestParam(defaultValue = "ALL") FilterType filterType, // "ALL" or "FOLLOWING"
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestParam(required = false) String keyword,
        @RequestParam(defaultValue = "ALL") Category category,
        @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {

        PostListResponseDto response = postService.getPosts(
            boardType, filterType, userDetails.getMember(), keyword, category, pageable
        );

        return ApiResponse.ok("게시글 다건 조회", response);
    }

    @Override
    @GetMapping("/{id}")
    public ApiResponse<PostReadResponseDto> getPost(
        @PathVariable Long id,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        PostReadResponseDto response = postService.getPostById(id, userDetails.getMember());


        return ApiResponse.ok("%d번 게시글 단건 조회".formatted(id), response);

    }

    @Override
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deletePost(
        @PathVariable Long id,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        postService.delete(id, userDetails.getMember());

        return ApiResponse.ok("%d번 게시글 삭제".formatted(id));

    }

    @Override
    @PostMapping
    public ApiResponse<Void> createPost(
        @RequestBody PostWriteRequestDto reqBody,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        postService.write(reqBody, userDetails.getMember());

        return ApiResponse.ok("게시글 생성");

    }

    @Override
    @PatchMapping("/{id}")
    public ApiResponse<Void> modifyPost(
        @PathVariable Long id,
        @RequestBody PostModifyRequestDto reqBody,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        postService.modify(id, reqBody, userDetails.getMember());

        return ApiResponse.ok("%d번 게시글 수정".formatted(id));
    }

}
