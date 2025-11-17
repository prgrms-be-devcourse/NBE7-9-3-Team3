package org.example.backend.domain.point.entity

import jakarta.persistence.*
import org.example.backend.domain.member.entity.Member
import java.time.LocalDateTime

@Entity
@Table(name = "point_log")
class Point private constructor( // 해당 클래스만 접근 가능한 생성자
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    val member: Member,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val type: TransactionType,

    @Column(nullable = false)
    val points: Long,

    @Column(nullable = false)
    val afterPoint: Long,

    @Column(nullable = false)
    val createDate: LocalDateTime = LocalDateTime.now()
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var logId: Long = 0L

    companion object {
        @JvmStatic
        fun create(member: Member, amount: Long, afterPoint: Long) = Point(
            member = member,
            type = TransactionType.CHARGE,
            points = amount,
            afterPoint = afterPoint
        )

        @JvmStatic
        fun createPurchase(member: Member, amount: Long, afterPoint: Long) = Point(
            member = member,
            type = TransactionType.PURCHASE,
            points = amount,
            afterPoint = afterPoint
        )

        @JvmStatic
        fun createSale(member: Member, amount: Long, afterPoint: Long) = Point(
            member = member,
            type = TransactionType.SALE,
            points = amount,
            afterPoint = afterPoint
        )
    }
}