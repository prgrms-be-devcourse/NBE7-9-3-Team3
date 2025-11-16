package org.example.backend.domain.tradecomment.controller

import jakarta.validation.Valid
import org.example.backend.domain.trade.enums.BoardType
import org.example.backend.domain.tradecomment.dto.TradeCommentDeleteRequestDto
import org.example.backend.domain.tradecomment.dto.TradeCommentRequestDto
import org.example.backend.domain.tradecomment.dto.TradeCommentResponseDto
import org.example.backend.domain.tradecomment.dto.TradeCommentUpdateRequestDto
import org.example.backend.domain.tradecomment.service.TradeCommentService
import org.example.backend.global.response.ApiResponse
import org.example.backend.global.security.CustomUserDetails
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/market/{boardType}/{tradeId}/comments")
class TradeCommentController(
    private val tradeCommentService: TradeCommentService
) : TradeCommentControllerSpec {

    @PostMapping
    override fun createComment(
        @PathVariable boardType: String,
        @PathVariable tradeId: Long,
        @RequestBody @Valid request: TradeCommentRequestDto
    ): ApiResponse<TradeCommentResponseDto> {
        val type = BoardType.from(boardType)
        val comment = tradeCommentService.createComment(type, request)
        return ApiResponse.ok("댓글 등록 성공", comment)
    }

    @GetMapping
    override fun getAllComments(
        @PathVariable boardType: String,
        @PathVariable tradeId: Long
    ): ApiResponse<List<TradeCommentResponseDto>> {
        val type = BoardType.from(boardType)
        val comments = tradeCommentService.getAllComment(type, tradeId)
        return ApiResponse.ok("댓글 목록 조회 성공", comments)
    }

    @PutMapping("/{commentId}")
    override fun updateComment(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @PathVariable boardType: String,
        @PathVariable tradeId: Long,
        @PathVariable commentId: Long,
        @RequestBody @Valid request: TradeCommentRequestDto
    ): ApiResponse<TradeCommentResponseDto> {
        val memberId = userDetails.getId()
        val type = BoardType.from(boardType)
        val updateRequest = TradeCommentUpdateRequestDto.of(
            type, tradeId, commentId, memberId, request
        )
        val comment = tradeCommentService.updateComment(updateRequest)
        return ApiResponse.ok("댓글 수정 성공", comment)
    }

    @DeleteMapping("/{commentId}")
    override fun deleteComment(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @PathVariable boardType: String,
        @PathVariable tradeId: Long,
        @PathVariable commentId: Long
    ): ApiResponse<Void> {
        val memberId = userDetails.getId()
        val type = BoardType.from(boardType)
        val deleteRequest = TradeCommentDeleteRequestDto.of(
            type, tradeId, commentId, memberId
        )
        tradeCommentService.deleteComment(deleteRequest)
        return ApiResponse.ok("댓글 삭제 성공")
    }
}
