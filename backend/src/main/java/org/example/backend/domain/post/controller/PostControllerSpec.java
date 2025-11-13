package org.example.backend.domain.post.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.backend.domain.post.dto.FilterType;
import org.example.backend.domain.post.dto.MyPostReadResponseDto;
import org.example.backend.domain.post.dto.PostListResponseDto;
import org.example.backend.domain.post.dto.PostModifyRequestDto;
import org.example.backend.domain.post.dto.PostReadResponseDto;
import org.example.backend.domain.post.dto.PostWriteRequestDto;
import org.example.backend.domain.post.entity.Post.BoardType;
import org.example.backend.domain.post.entity.Post.Category;
import org.example.backend.global.response.ApiResponse;
import org.example.backend.global.security.CustomUserDetails;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "Post", description = "질문/자랑 게시판 관리 API")
public interface PostControllerSpec {

    @Operation(summary = "내 게시글 조회", description = "사용자가 작성한 게시글 목록을 조회합니다.")
    ApiResponse<List<MyPostReadResponseDto>> getMyPosts(
            @RequestParam BoardType boardType,
            @AuthenticationPrincipal CustomUserDetails userDetails
    );

    @Operation(summary = "게시글 목록 조회", description = "게시판의 게시글 목록을 조회합니다.")
    ApiResponse<PostListResponseDto> getPosts(
            @RequestParam BoardType boardType,
            @RequestParam(defaultValue = "ALL") FilterType filterType,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "ALL") Category category,
            Pageable pageable
    );

    @Operation(summary = "게시글 조회", description = "특정 게시글의 상세 정보를 조회합니다.")
    ApiResponse<PostReadResponseDto> getPost(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    );

    @Operation(summary = "게시글 삭제", description = "게시글을 삭제합니다.")
    ApiResponse<Void> deletePost(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    );

    @Operation(summary = "게시글 생성", description = "새로운 게시글을 생성합니다.")
    ApiResponse<Void> createPost(
            @RequestBody PostWriteRequestDto reqBody,
            @AuthenticationPrincipal CustomUserDetails userDetails
    );

    @Operation(summary = "게시글 수정", description = "기존 게시글을 수정합니다.")
    ApiResponse<Void> modifyPost(
            @PathVariable Long id,
            @RequestBody PostModifyRequestDto reqBody,
            @AuthenticationPrincipal CustomUserDetails userDetails
    );
}
