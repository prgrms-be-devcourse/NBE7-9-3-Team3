package org.example.backend.domain.tradecomment.dto

data class MyTradeCommentReadResponseDto(
    val id: Long,
    val tradeId: Long,
    val tradeTitle: String,
    val content: String,
    val boardType: String
)
