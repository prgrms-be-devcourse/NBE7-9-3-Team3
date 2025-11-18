package org.example.backend.domain.tradechat.dto

import org.example.backend.domain.tradechat.entity.TradeChatMessage
import java.time.LocalDateTime

@JvmRecord
data class TradeChatMessageDto(
    val messageId: Long,
    val senderId: Long?,
    val senderNickname: String?,
    val content: String,
    val sendDate: LocalDateTime?
) {
    companion object {
        @JvmStatic
        fun from(message: TradeChatMessage): TradeChatMessageDto {
            return TradeChatMessageDto(
                message.id,
                message.sender.memberId,
                message.sender.nickname,
                message.content,
                message.sendDate
            )
        }
    }
}
