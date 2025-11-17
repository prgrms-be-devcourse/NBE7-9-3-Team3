package org.example.backend.domain.trade.entity

import jakarta.persistence.*
import org.example.backend.domain.member.entity.Member
import org.example.backend.domain.trade.enums.BoardType
import org.example.backend.domain.trade.enums.TradeStatus
import org.example.backend.domain.tradecomment.entity.TradeComment
import java.time.LocalDateTime

@Entity
@Table(
    name = "trade",
    indexes = [
        Index(name = "idx_board_type_create_date", columnList = "board_type, create_date"),
        Index(name = "idx_board_type_status", columnList = "board_type, status"),
        Index(name = "idx_board_type_price", columnList = "board_type, price"),
        Index(name = "idx_member_board_type", columnList = "member_id, board_type")]
)
class Trade(
    @JoinColumn(name = "member_id")
    @ManyToOne(fetch = FetchType.LAZY)
    val member: Member,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var boardType: BoardType,

    @Column(nullable = false, length = 50)
    var title: String,

    @Column(nullable = false)
    var description: String,

    @Column(nullable = false)
    var price: Long,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var status: TradeStatus,

    @Column(length = 20)
    var category: String? = null,

    val createDate: LocalDateTime = LocalDateTime.now()
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val tradeId: Long = 0L

    @OneToMany(mappedBy = "trade", cascade = [CascadeType.ALL], orphanRemoval = true)
    private val images: MutableList<TradeImage> = mutableListOf()

    @OneToMany(mappedBy = "trade", cascade = [CascadeType.ALL], orphanRemoval = true)
    private val comments: MutableList<TradeComment> = mutableListOf()

    fun update(
        title: String = this.title,
        description: String = this.description,
        price: Long = this.price,
        status: TradeStatus = this.status,
        category: String? = this.category
    ) {
        this.title = title
        this.description = description
        this.price = price
        this.status = status
        this.category = category
    }

    fun addImage(imageUrl: String) {
        val tradeImage = TradeImage.of(this, imageUrl)
        images.add(tradeImage)
    }

    fun clearImages() {
        images.clear()
    }

    val imageUrls: List<String>
        get() = images.map { it.image }

    fun completeTransaction() {
        status = TradeStatus.COMPLETED
    }

    val isSoldOut: Boolean
        get() = status == TradeStatus.COMPLETED
}