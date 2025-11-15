package org.example.backend.domain.trade.dto;

import java.time.LocalDateTime;
import java.util.List;
import org.example.backend.domain.trade.entity.Trade;
import org.example.backend.domain.trade.enums.BoardType;
import org.example.backend.domain.trade.enums.TradeStatus;

public record TradeResponseDto(
    Long tradeId,
    Long memberId,
    String memberNickname,
    BoardType boardType,
    String title,
    String description,
    Long price,
    TradeStatus status,
    String category,
    LocalDateTime createdDate,
    List<String> images
) {

    public static TradeResponseDto from(Trade trade) {
        return new TradeResponseDto(
            trade.getTradeId(),
            trade.getMember().getMemberId(),
            trade.getMember().getNickname(),
            trade.getBoardType(),
            trade.getTitle(),
            trade.getDescription(),
            trade.getPrice(),
            trade.getStatus(),
            trade.getCategory(),
            trade.getCreateDate(),
            trade.getImageUrls()
        );
    }
}
