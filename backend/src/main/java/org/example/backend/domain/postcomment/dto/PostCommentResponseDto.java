package org.example.backend.domain.postcomment.dto;

import org.example.backend.domain.postcomment.entity.PostComment;

public record PostCommentResponseDto(
    Long id,
    Long postId,
    String content,
    String nickname
) {
    public PostCommentResponseDto(PostComment postComment) {
        this(
            postComment.getId(),
            postComment.getPost().getId(),
            postComment.getContent(),
            postComment.getAuthor().getNickname()
        );
    }
}
