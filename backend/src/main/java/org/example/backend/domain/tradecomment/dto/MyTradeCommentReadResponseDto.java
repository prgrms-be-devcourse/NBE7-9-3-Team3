package org.example.backend.domain.tradecomment.dto;

public record MyTradeCommentReadResponseDto(
    Long id,
    Long tradeId,
    String tradeTitle,
    String content,
    String boardType
) {}
