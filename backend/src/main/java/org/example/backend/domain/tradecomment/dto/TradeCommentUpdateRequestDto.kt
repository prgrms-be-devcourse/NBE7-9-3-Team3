package org.example.backend.domain.tradecomment.dto

import org.example.backend.domain.trade.enums.BoardType

data class TradeCommentUpdateRequestDto(
    @JvmField val boardType: BoardType,
    @JvmField val tradeId: Long,
    @JvmField val commentId: Long,
    @JvmField val memberId: Long,
    @JvmField val commentData: TradeCommentRequestDto
) {
    companion object {
        @JvmStatic
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
