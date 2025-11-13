package org.example.backend.domain.tradecomment.dto;

import java.time.LocalDateTime;
import org.example.backend.domain.tradecomment.entity.TradeComment;

public record TradeCommentResponseDto(
    Long commentId,
    Long memberId,
    String memberNickname,
    Long tradeId,
    String comment,
    LocalDateTime createdDate
) {

    public static TradeCommentResponseDto from(TradeComment comment) {
        return new TradeCommentResponseDto(
            comment.getCommentId(),
            comment.getMember().getMemberId(),
            comment.getMember().getNickname(),
            comment.getTrade().getTradeId(),
            comment.getContent(),
            comment.getCreateDate()
        );
    }

}
