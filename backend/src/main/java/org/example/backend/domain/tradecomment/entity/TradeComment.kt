package org.example.backend.domain.tradecomment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.backend.domain.member.entity.Member;
import org.example.backend.domain.trade.entity.Trade;

@NoArgsConstructor
@Getter
@Entity
public class TradeComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trade_id", nullable = false)
    private Trade trade;

    @Column(nullable = false)
    private String content;

    private LocalDateTime createDate;

    public TradeComment(Member member, Trade trade, String content) {
        this.member = member;
        this.trade = trade;
        this.content = content;
        this.createDate = LocalDateTime.now();
    }

    public void update(String content) {
        this.content = content;
    }


}
