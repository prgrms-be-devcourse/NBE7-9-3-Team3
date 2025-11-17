package org.example.backend.domain.trade.dto

import org.example.backend.domain.trade.enums.BoardType

data class TradeUpdateRequestDto(
    val boardType: BoardType,
    val tradeId: Long,
    val memberId: Long,
    val tradeData: TradeRequestDto,
    val imageUrls: List<String>
) {
    companion object {
        fun of(
            boardType: BoardType,
            tradeId: Long,
            memberId: Long,
            tradeData: TradeRequestDto,
            imageUrls: List<String>
        ): TradeUpdateRequestDto {
            return TradeUpdateRequestDto(
                boardType = boardType,
                tradeId = tradeId,
                memberId = memberId,
                tradeData = tradeData,
                imageUrls = imageUrls
            )
        }
    }
}
