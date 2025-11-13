package org.example.backend.domain.tradechat.dto;

import org.example.backend.domain.tradechat.entity.TradeChatMessage;

import java.time.LocalDateTime;

public record TradeChatMessageDto(
        Long messageId,
        Long senderId,
        String senderNickname,
        String content,
        LocalDateTime sendDate
) {
    public static TradeChatMessageDto from(TradeChatMessage message) {
        return new TradeChatMessageDto(
                message.getId(),
                message.getSender().getMemberId(),
                message.getSender().getNickname(),
                message.getContent(),
                message.getSendDate()
        );
    }
}
