package org.example.backend.domain.trade.dto

import org.example.backend.domain.trade.entity.Trade
import org.example.backend.domain.trade.enums.BoardType
import org.example.backend.domain.trade.enums.TradeStatus
import java.time.LocalDateTime

data class TradeResponseDto(
    val tradeId: Long,
    val memberId: Long,
    val memberNickname: String,
    val boardType: BoardType,
    val title: String,
    val description: String,
    val price: Long,
    val status: TradeStatus,
    val category: String?,
    val createdDate: LocalDateTime,
    val images: List<String>
) {
    companion object {
        @JvmStatic
        fun from(trade: Trade): TradeResponseDto {
            return TradeResponseDto(
                tradeId = trade.tradeId,
                memberId = 1L,  // TODO: trade.member.memberId
                memberNickname = "TODO",    // TODO: trade.member.nickname
                boardType = trade.boardType,
                title = trade.title,
                description = trade.description,
                price = trade.price,
                status = trade.status,
                category = trade.category,
                createdDate = trade.createDate,
                images = trade.imageUrls
            )
        }
    }
}
