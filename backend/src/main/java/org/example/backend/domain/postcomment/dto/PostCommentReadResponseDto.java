package org.example.backend.domain.postcomment.dto;

public record PostCommentReadResponseDto(
    Long id,
    String content,
    String nickname,
    boolean isMine
) {}
