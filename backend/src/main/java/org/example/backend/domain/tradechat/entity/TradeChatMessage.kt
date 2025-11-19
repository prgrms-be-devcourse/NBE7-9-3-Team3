package org.example.backend.domain.tradechat.entity

import jakarta.persistence.*
import org.example.backend.domain.member.entity.Member
import java.time.LocalDateTime

@Entity
@Table(name = "trade_chat_message")
class TradeChatMessage private constructor(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    val chatRoom: TradeChatRoom,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    val sender: Member,

    @Column(nullable = false)
    val content: String,

    @Column(nullable = false)
    val sendDate: LocalDateTime = LocalDateTime.now()
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L
        private set

    companion object {
        // 메세지 채팅 생성
        fun create(chatRoom: TradeChatRoom, sender: Member, content: String): TradeChatMessage =
            TradeChatMessage(
                chatRoom = chatRoom,
                sender = sender,
                content = content
            )
    }
}
