package org.example.backend.domain.tradecomment.dto

import org.example.backend.domain.trade.enums.BoardType

data class TradeCommentUpdateRequestDto(
    val boardType: BoardType,
    val tradeId: Long,
    val commentId: Long,
    val memberId: Long,
    val commentData: TradeCommentRequestDto
) {
    companion object {
        fun of(
            boardType: BoardType,
            tradeId: Long,
            commentId: Long,
            memberId: Long,
            commentData: TradeCommentRequestDto
        ): TradeCommentUpdateRequestDto {
            return TradeCommentUpdateRequestDto(
                boardType = boardType,
                tradeId = tradeId,
                commentId = commentId,
                memberId = memberId,
                commentData = commentData
            )
        }
    }
}
