package org.example.backend.domain.postcomment.dto;

import jakarta.validation.constraints.NotBlank;

public record PostCommentCreateRequestDto(

    @NotBlank(message = "댓글 내용은 필수입니다.")
    String content,
    Long postId
) {}
