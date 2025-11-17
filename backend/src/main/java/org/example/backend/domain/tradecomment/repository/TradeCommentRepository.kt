package org.example.backend.domain.tradecomment.repository

import org.example.backend.domain.member.entity.Member
import org.example.backend.domain.tradecomment.entity.TradeComment
import org.springframework.data.jpa.repository.JpaRepository

interface TradeCommentRepository : JpaRepository<TradeComment, Long> {
    fun findByTradeTradeId(tradeId: Long): List<TradeComment>

    fun findByMember(member: Member): List<TradeComment>
}
