package org.example.backend.domain.tradecomment.dto

import org.example.backend.domain.tradecomment.entity.TradeComment
import java.time.LocalDateTime

data class TradeCommentResponseDto(
    val commentId: Long,
    val memberId: Long,
    val memberNickname: String,
    val tradeId: Long,
    val comment: String,
    val createDate: LocalDateTime
) {
    companion object {
        fun from(comment: TradeComment) = TradeCommentResponseDto(
            commentId = comment.commentId,
            memberId = 1L,  // TODO: comment.member.memberId
            memberNickname = "테스트",  // TODO: comment.member.nickname
            tradeId = comment.trade.tradeId,
            comment = comment.content,
            createDate = comment.createDate
        )
    }
}
