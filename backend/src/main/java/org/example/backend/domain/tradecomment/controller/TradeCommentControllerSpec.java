package org.example.backend.domain.tradecomment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.backend.domain.tradecomment.dto.TradeCommentRequestDto;
import org.example.backend.domain.tradecomment.dto.TradeCommentResponseDto;
import org.example.backend.global.response.ApiResponse;
import org.example.backend.global.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "Trade Comment", description = "거래 게시글 댓글 관리 API")
public interface TradeCommentControllerSpec {

    @Operation(summary = "댓글 등록", description = "거래 게시글에 새로운 댓글을 등록합니다.")
    ApiResponse<TradeCommentResponseDto> createComment(
            @Parameter(description = "게시판 타입 (FISH: 물고기, SECONDHAND: 중고물품)", required = true)
            @PathVariable String boardType,
            @Parameter(description = "거래 게시글 ID", required = true)
            @PathVariable Long tradeId,
            @Parameter(description = "댓글 정보", required = true)
            @RequestBody TradeCommentRequestDto request
    );

    @Operation(summary = "댓글 목록 조회", description = "특정 거래 게시글의 모든 댓글을 조회합니다.")
    ApiResponse<List<TradeCommentResponseDto>> getAllComments(
            @Parameter(description = "게시판 타입 (FISH: 물고기, SECONDHAND: 중고물품)", required = true)
            @PathVariable String boardType,
            @Parameter(description = "거래 게시글 ID", required = true)
            @PathVariable Long tradeId
    );

    @Operation(summary = "댓글 수정", description = "기존 댓글을 수정합니다.")
    ApiResponse<TradeCommentResponseDto> updateComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "게시판 타입 (FISH: 물고기, SECONDHAND: 중고물품)", required = true)
            @PathVariable String boardType,
            @Parameter(description = "거래 게시글 ID", required = true)
            @PathVariable Long tradeId,
            @Parameter(description = "댓글 ID", required = true)
            @PathVariable Long commentId,
            @Parameter(description = "댓글 수정 정보", required = true)
            @RequestBody TradeCommentRequestDto request
    );

    @Operation(summary = "댓글 삭제", description = "기존 댓글을 삭제합니다.")
    ApiResponse<Void> deleteComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "게시판 타입 (FISH: 물고기, SECONDHAND: 중고물품)", required = true)
            @PathVariable String boardType,
            @Parameter(description = "거래 게시글 ID", required = true)
            @PathVariable Long tradeId,
            @Parameter(description = "댓글 ID", required = true)
            @PathVariable Long commentId
    );
}
