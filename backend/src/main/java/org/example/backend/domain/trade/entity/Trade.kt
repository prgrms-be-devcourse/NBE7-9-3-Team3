package org.example.backend.domain.trade.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.backend.domain.member.entity.Member;
import org.example.backend.domain.trade.enums.BoardType;
import org.example.backend.domain.trade.enums.TradeStatus;
import org.example.backend.domain.tradecomment.entity.TradeComment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
@Entity
@Table(name = "trade", indexes = {
    @Index(name = "idx_board_type_create_date", columnList = "board_type, create_date"),
    @Index(name = "idx_board_type_status", columnList = "board_type, status"),
    @Index(name = "idx_board_type_price", columnList = "board_type, price"),
    @Index(name = "idx_member_board_type", columnList = "member_id, board_type")
})
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tradeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BoardType boardType;

    @Column(nullable = false, length = 50)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Long price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TradeStatus status;

    @Column(length = 20)
    private String category;

    private LocalDateTime createDate;

    @OneToMany(mappedBy = "trade", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TradeImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "trade", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TradeComment> comments = new ArrayList<>();

    public Trade(Member member, BoardType boardType, String title, String description,
        Long price, TradeStatus status, String category, LocalDateTime createDate) {
        this.member = member;
        this.boardType = boardType;
        this.title = title;
        this.description = description;
        this.price = price;
        this.status = status;
        this.category = category;
        this.createDate = createDate;
    }

    public void update(String title, String description, Long price, TradeStatus status,
        String category) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.status = status;
        this.category = category;
    }

    public void addImage(String imageUrl) {
        TradeImage tradeImage = TradeImage.of(this, imageUrl);
        this.images.add(tradeImage);
    }

    public void clearImages() {
        this.images.clear();
    }

    public List<String> getImageUrls() {
        return images.stream()
            .map(TradeImage::getImage)
            .toList();
    }

    public void completeTransaction() {
        this.status = TradeStatus.COMPLETED;
    }

    public boolean isSoldOut() {
        return this.status == TradeStatus.COMPLETED;
    }
}