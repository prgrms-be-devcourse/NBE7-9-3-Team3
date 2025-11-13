package org.example.backend.domain.postcomment.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.backend.domain.postcomment.dto.MyPostCommentReadResponseDto;
import org.example.backend.domain.postcomment.dto.PostCommentCreateRequestDto;
import org.example.backend.domain.postcomment.dto.PostCommentModifyRequestDto;
import org.example.backend.domain.postcomment.dto.PostCommentReadResponseDto;
import org.example.backend.domain.postcomment.service.PostCommentService;
import org.example.backend.global.response.ApiResponse;
import org.example.backend.global.security.CustomUserDetails;
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
@RequestMapping("/api/posts/comments")
public class PostCommentController implements PostCommentControllerSpec {

    private final PostCommentService postCommentService;

    @Override
    @GetMapping
    public ApiResponse<List<PostCommentReadResponseDto>> getPostComments(
        @RequestParam Long postId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        List<PostCommentReadResponseDto> response =
            postCommentService.getPostComments(postId, userDetails.getMember());

        return ApiResponse.ok("댓글 목록 조회", response);
    }

    @Override
    @GetMapping("/my")
    public ApiResponse<List<MyPostCommentReadResponseDto>> getMyPostComments(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        List<MyPostCommentReadResponseDto> response = postCommentService.findMyComments(userDetails.getMember());
        return new ApiResponse<>("200-1",
            "내가 쓴 댓글 목록 조회",
            response
        );
    }

    @Override
    @DeleteMapping("/{commentId}")
    public ApiResponse<Void> deletePostComment(
        @PathVariable Long commentId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        postCommentService.deletePostComment(commentId, userDetails.getMember());

        return ApiResponse.ok("%d번 댓글 삭제".formatted(commentId));

    }

    @Override
    @PostMapping
    public ApiResponse<Void> createPostComment(
        @RequestBody PostCommentCreateRequestDto reqBody,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        postCommentService.createPostComment(reqBody, userDetails.getMember());


        return ApiResponse.ok("댓글이 생성되었습니다");

    }

    @Override
    @PatchMapping("/{commentId}")
    public ApiResponse<Void> modifyItem(
        @PathVariable Long commentId,
        @RequestBody PostCommentModifyRequestDto reqBody,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ){

        postCommentService.modifyPostComment(commentId, reqBody, userDetails.getMember());

        return ApiResponse.ok("%d번 댓글 수정".formatted(commentId));
    }
}
