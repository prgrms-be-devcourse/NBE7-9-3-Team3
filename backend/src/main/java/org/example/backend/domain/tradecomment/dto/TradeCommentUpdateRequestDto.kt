package org.example.backend.domain.tradecomment.dto;

import org.example.backend.domain.trade.enums.BoardType;

public record TradeCommentUpdateRequestDto(
    BoardType boardType,
    Long tradeId,
    Long commentId,
    Long memberId,
    TradeCommentRequestDto commentData
) {
    public static TradeCommentUpdateRequestDto of(
        BoardType boardType,
        Long tradeId,
        Long commentId,
        Long memberId,
        TradeCommentRequestDto commentData
    ) {
        return new TradeCommentUpdateRequestDto(boardType, tradeId, commentId, memberId, commentData);
    }
}
