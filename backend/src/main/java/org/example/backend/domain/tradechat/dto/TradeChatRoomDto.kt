package org.example.backend.domain.tradechat.dto

import org.example.backend.domain.trade.enums.BoardType
import org.example.backend.domain.tradechat.entity.ChatStatus
import org.example.backend.domain.tradechat.entity.TradeChatRoom
import java.time.LocalDateTime

data class TradeChatRoomDto(
    val roomId: Long,
    val tradeId: Long,
    val tradeTitle: String,
    val boardType: BoardType,
    val sellerId: Long?,
    val sellerNickname: String,
    val buyerId: Long?,
    val buyerNickname: String,
    val createDate: LocalDateTime,
    val status: ChatStatus
) {
    companion object {
        fun from(room: TradeChatRoom): TradeChatRoomDto {
            return TradeChatRoomDto(
                room.id,
                room.trade.tradeId,
                room.trade.title,
                room.trade.boardType,
                room.sellerId.memberId,
                room.sellerId.nickname,
                room.buyerId.memberId,
                room.buyerId.nickname,
                room.createDate,
                room.status
            )
        }
    }
}
