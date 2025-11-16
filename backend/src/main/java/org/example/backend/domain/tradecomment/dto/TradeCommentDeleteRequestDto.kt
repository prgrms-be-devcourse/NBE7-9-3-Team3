package org.example.backend.domain.tradecomment.dto;

import org.example.backend.domain.trade.enums.BoardType;

public record TradeCommentDeleteRequestDto(
    BoardType boardType,
    Long tradeId,
    Long commentId,
    Long memberId
) {
    public static TradeCommentDeleteRequestDto of(
        BoardType boardType,
        Long tradeId,
        Long commentId,
        Long memberId
    ) {
        return new TradeCommentDeleteRequestDto(boardType, tradeId, commentId, memberId);
    }
}
