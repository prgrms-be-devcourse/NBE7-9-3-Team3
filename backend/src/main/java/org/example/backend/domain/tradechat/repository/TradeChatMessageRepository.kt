package org.example.backend.domain.tradechat.repository

import org.example.backend.domain.tradechat.entity.TradeChatMessage
import org.example.backend.domain.tradechat.entity.TradeChatRoom
import org.springframework.data.jpa.repository.JpaRepository

interface TradeChatMessageRepository : JpaRepository<TradeChatMessage, Long> {
    fun findByChatRoomOrderBySendDateAsc(room: TradeChatRoom): MutableList<TradeChatMessage>
}

