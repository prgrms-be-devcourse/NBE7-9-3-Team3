package org.example.backend.domain.postcomment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.backend.domain.postcomment.dto.MyPostCommentReadResponseDto;
import org.example.backend.domain.postcomment.dto.PostCommentCreateRequestDto;
import org.example.backend.domain.postcomment.dto.PostCommentModifyRequestDto;
import org.example.backend.domain.postcomment.dto.PostCommentReadResponseDto;
import org.example.backend.global.response.ApiResponse;
import org.example.backend.global.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "PostComment", description = "질문/자랑 게시판 댓글 관리 API")
public interface PostCommentControllerSpec {

    @Operation(summary = "게시글 댓글 조회", description = "특정 게시글의 댓글 목록을 조회합니다.")
    ApiResponse<List<PostCommentReadResponseDto>> getPostComments(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    );

    @Operation(summary = "내 댓글 조회", description = "내가 작성한 댓글 목록을 조회합니다.")
    ApiResponse<List<MyPostCommentReadResponseDto>> getMyPostComments(
            @AuthenticationPrincipal CustomUserDetails userDetails
    );

    @Operation(summary = "댓글 삭제", description = "기존 댓글을 삭제합니다.")
    ApiResponse<Void> deletePostComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    );

    @Operation(summary = "댓글 생성", description = "새로운 댓글을 생성합니다.")
    ApiResponse<Void> createPostComment(
            @RequestBody PostCommentCreateRequestDto reqBody,
            @AuthenticationPrincipal CustomUserDetails userDetails
    );

    @Operation(summary = "댓글 수정", description = "기존 댓글을 수정합니다.")
    ApiResponse<Void> modifyItem(
            @PathVariable Long commentId,
            @RequestBody PostCommentModifyRequestDto reqBody,
            @AuthenticationPrincipal CustomUserDetails userDetails
    );
}
