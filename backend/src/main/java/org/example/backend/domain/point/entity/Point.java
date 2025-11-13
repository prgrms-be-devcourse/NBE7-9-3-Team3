package org.example.backend.domain.point.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.backend.domain.member.entity.Member;

import java.time.LocalDateTime;

@Entity
@Table(name = "point_log")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Point {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type = TransactionType.CHARGE;

    @Column(nullable = false)
    private Long points = 0L;

    @Column(nullable = false)
    private Long afterPoint = 0L;

    @Column(nullable = false)
    private LocalDateTime createDate;

    // 내역 생성 - 충전
    public static Point create(Member member, long amount, long afterPoint) {
        Point point = new Point();
        point.member = member;
        point.type = TransactionType.CHARGE;
        point.points = amount;
        point.afterPoint = afterPoint;
        point.createDate = LocalDateTime.now();
        return point;
    }

    // 내역 생성 - 구매
    public static Point createPurchase(Member member, long amount, long afterPoint) {
        Point point = new Point();
        point.member = member;
        point.type = TransactionType.PURCHASE;
        point.points = amount;
        point.afterPoint = afterPoint;
        point.createDate = LocalDateTime.now();
        return point;
    }

    // 내역 생성 - 판매
    public static Point createSale(Member member, long amount, long afterPoint) {
        Point point = new Point();
        point.member = member;
        point.type = TransactionType.SALE;
        point.points = amount;
        point.afterPoint = afterPoint;
        point.createDate = LocalDateTime.now();
        return point;
    }
}
