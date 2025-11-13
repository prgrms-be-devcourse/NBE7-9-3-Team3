package org.example.backend.domain.trade.dto;

import java.util.List;
import org.example.backend.domain.trade.enums.BoardType;

public record TradeUpdateRequestDto(
    BoardType boardType,
    Long tradeId,
    Long memberId,
    TradeRequestDto tradeData,
    List<String> imageUrls
) {

    public static TradeUpdateRequestDto of(
        BoardType boardType,
        Long tradeId,
        Long memberId,
        TradeRequestDto tradeData,
        List<String> imageUrls
    ) {
        return new TradeUpdateRequestDto(boardType, tradeId, memberId, tradeData, imageUrls);
    }
}
