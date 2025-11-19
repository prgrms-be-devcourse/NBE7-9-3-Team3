package org.example.backend.domain.tradechat.event

import org.example.backend.domain.tradechat.dto.TradeChatMessageDto

data class TradeChatMessageEvent(
    val roomId: Long,
    val messageDto: TradeChatMessageDto
)
