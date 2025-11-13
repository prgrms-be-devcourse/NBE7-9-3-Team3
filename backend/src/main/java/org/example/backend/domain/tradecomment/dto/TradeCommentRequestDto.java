package org.example.backend.domain.tradecomment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.backend.domain.member.entity.Member;
import org.example.backend.domain.trade.entity.Trade;
import org.example.backend.domain.tradecomment.entity.TradeComment;

public record TradeCommentRequestDto(
    @NotNull long memberId,
    @NotNull long tradeId,
    @NotBlank String content
) {

    public TradeComment toEntity(Member member, Trade trade) {
        return new TradeComment(
            member,
            trade,
            this.content
        );
    }
}
