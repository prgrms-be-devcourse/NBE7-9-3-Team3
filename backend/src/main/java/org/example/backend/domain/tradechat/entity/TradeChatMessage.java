package org.example.backend.domain.tradechat.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.backend.domain.member.entity.Member;

import java.time.LocalDateTime;

@Entity
@Table(name = "trade_chat_message")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TradeChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column (name = "message_id", nullable = false)
    private Long Id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "room_id", nullable = false)
    private TradeChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "sender_id", nullable = false)
    private Member sender;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private LocalDateTime sendDate;

    // 메세지 채팅 생성
    public static TradeChatMessage create(TradeChatRoom chatRoom, Member sender, String content) {
        TradeChatMessage message = new TradeChatMessage();
        message.chatRoom = chatRoom;
        message.sender = sender;
        message.content = content;
        message.sendDate = LocalDateTime.now();
        return message;
    }
}
