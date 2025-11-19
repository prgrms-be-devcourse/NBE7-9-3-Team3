package org.example.backend.domain.tradechat.entity

import jakarta.persistence.*
import org.example.backend.domain.member.entity.Member
import org.example.backend.domain.trade.entity.Trade
import org.springframework.data.annotation.CreatedDate
import java.time.LocalDateTime

@Entity
@Table(name = "trade_chat_room")
class TradeChatRoom private constructor(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trade_id", nullable = false)
    val trade: Trade,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    val sellerId: Member,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    val buyerId: Member,

    @CreatedDate
    val createDate: LocalDateTime = LocalDateTime.now(),

    @Enumerated(EnumType.STRING)
    val status: ChatStatus = ChatStatus.ONGOING  // 기본 상태 예시
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L
        private set

    companion object {
        fun create(
            trade: Trade,
            seller: Member,
            buyer: Member,
            status: ChatStatus = ChatStatus.ONGOING
        ): TradeChatRoom = TradeChatRoom(
            trade = trade,
            sellerId = seller,
            buyerId = buyer,
            status = status
        )
    }
}
