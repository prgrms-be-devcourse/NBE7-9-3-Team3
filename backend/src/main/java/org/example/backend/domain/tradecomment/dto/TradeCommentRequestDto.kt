package org.example.backend.domain.tradecomment.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.example.backend.domain.member.entity.Member
import org.example.backend.domain.trade.entity.Trade
import org.example.backend.domain.tradecomment.entity.TradeComment

data class TradeCommentRequestDto(
    @field:NotNull @JvmField val memberId: Long,
    @field:NotNull @JvmField val tradeId: Long,
    @field:NotBlank @JvmField val content: String
) {
    fun toEntity(member: Member, trade: Trade): TradeComment {
        return TradeComment(
            member = member,
            trade = trade,
            content = content
        )
    }
}
