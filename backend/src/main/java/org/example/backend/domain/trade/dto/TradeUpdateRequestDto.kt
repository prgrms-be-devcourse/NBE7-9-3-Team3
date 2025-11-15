package org.example.backend.domain.trade.dto

import org.example.backend.domain.trade.enums.BoardType

data class TradeUpdateRequestDto(
    @JvmField val boardType: BoardType,
    @JvmField val tradeId: Long,
    @JvmField val memberId: Long,
    @JvmField val tradeData: TradeRequestDto,
    @JvmField val imageUrls: List<String>
) {
    companion object {
        @JvmStatic
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
