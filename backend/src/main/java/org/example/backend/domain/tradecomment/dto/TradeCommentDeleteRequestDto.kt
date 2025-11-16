package org.example.backend.domain.tradecomment.dto

import org.example.backend.domain.trade.enums.BoardType

data class TradeCommentDeleteRequestDto(
    @JvmField val boardType: BoardType,
    @JvmField val tradeId: Long,
    @JvmField val commentId: Long,
    @JvmField val memberId: Long
) {
    companion object {
        @JvmStatic
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
