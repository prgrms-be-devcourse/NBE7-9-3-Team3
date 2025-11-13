package org.example.backend.domain.tradecomment.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.backend.domain.trade.enums.BoardType;
import org.example.backend.domain.tradecomment.dto.TradeCommentDeleteRequestDto;
import org.example.backend.domain.tradecomment.dto.TradeCommentRequestDto;
import org.example.backend.domain.tradecomment.dto.TradeCommentResponseDto;
import org.example.backend.domain.tradecomment.dto.TradeCommentUpdateRequestDto;
import org.example.backend.domain.tradecomment.service.TradeCommentService;
import org.example.backend.global.response.ApiResponse;
import org.example.backend.global.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/market/{boardType}/{tradeId}/comments")
@RequiredArgsConstructor
public class TradeCommentController implements TradeCommentControllerSpec {

    private final TradeCommentService tradeCommentService;

    @Override
    @PostMapping
    public ApiResponse<TradeCommentResponseDto> createComment(
        @PathVariable String boardType,
        @PathVariable Long tradeId,
        @Valid @RequestBody TradeCommentRequestDto request) {
        BoardType type = BoardType.from(boardType);
        TradeCommentResponseDto comment = tradeCommentService.createComment(type, request);
        return ApiResponse.ok("댓글 등록 성공", comment);
    }

    @Override
    @GetMapping
    public ApiResponse<List<TradeCommentResponseDto>> getAllComments(
        @PathVariable String boardType,
        @PathVariable Long tradeId) {
        BoardType type = BoardType.from(boardType);
        List<TradeCommentResponseDto> comments = tradeCommentService.getAllComment(type, tradeId);
        return ApiResponse.ok("댓글 목록 조회 성공", comments);
    }

    @Override
    @PutMapping("/{commentId}")
    public ApiResponse<TradeCommentResponseDto> updateComment(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable String boardType,
        @PathVariable Long tradeId,
        @PathVariable Long commentId,
        @Valid @RequestBody TradeCommentRequestDto request) {
        Long memberId = userDetails.getId();
        BoardType type = BoardType.from(boardType);
        TradeCommentUpdateRequestDto updateRequest = TradeCommentUpdateRequestDto.of(
            type, tradeId, commentId, memberId, request
        );
        TradeCommentResponseDto comment = tradeCommentService.updateComment(updateRequest);
        return ApiResponse.ok("댓글 수정 성공", comment);
    }

    @Override
    @DeleteMapping("/{commentId}")
    public ApiResponse<Void> deleteComment(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable String boardType,
        @PathVariable Long tradeId,
        @PathVariable Long commentId) {
        Long memberId = userDetails.getId();
        BoardType type = BoardType.from(boardType);
        TradeCommentDeleteRequestDto deleteRequest = TradeCommentDeleteRequestDto.of(
            type, tradeId, commentId, memberId
        );
        tradeCommentService.deleteComment(deleteRequest);
        return ApiResponse.ok("댓글 삭제 성공");
    }

}
