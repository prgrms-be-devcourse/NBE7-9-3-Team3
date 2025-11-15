package org.example.backend.domain.trade.entity

import jakarta.persistence.*

@Entity
class TradeImage(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trade_id")
    val trade: Trade,

    @Column(nullable = false, length = 500)
    val image: String
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val imageId: Long = 0L

    companion object {
        fun of(trade: Trade, imageUrl: String): TradeImage {
            requireNotNull(imageUrl)
            return TradeImage(trade, imageUrl)
        }
    }
}
