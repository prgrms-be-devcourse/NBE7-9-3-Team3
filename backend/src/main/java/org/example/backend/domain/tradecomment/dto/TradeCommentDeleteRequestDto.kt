package org.example.backend.domain.tradecomment.dto

import org.example.backend.domain.trade.enums.BoardType

data class TradeCommentDeleteRequestDto(
    val boardType: BoardType,
    val tradeId: Long,
    val commentId: Long,
    val memberId: Long
) {
    companion object {
        fun of(
            boardType: BoardType,
            tradeId: Long,
            commentId: Long,
            memberId: Long
        ): TradeCommentDeleteRequestDto {
            return TradeCommentDeleteRequestDto(
                boardType = boardType,
                tradeId = tradeId,
                commentId = commentId,
                memberId = memberId
            )
        }
    }
}
