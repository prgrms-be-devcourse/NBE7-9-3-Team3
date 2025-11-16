package org.example.backend.domain.tradecomment.entity

import jakarta.persistence.*
import org.example.backend.domain.member.entity.Member
import org.example.backend.domain.trade.entity.Trade
import java.time.LocalDateTime

@Entity
class TradeComment @JvmOverloads constructor(
    @JoinColumn(name = "member_id")
    @ManyToOne(fetch = FetchType.LAZY)
    var member: Member,

    @JoinColumn(name = "trade_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    var trade: Trade,

    @Column(nullable = false)
    var content: String,

    val createDate: LocalDateTime = LocalDateTime.now()
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val commentId: Long = 0L

    fun update(content: String) {
        this.content = content
    }
}
