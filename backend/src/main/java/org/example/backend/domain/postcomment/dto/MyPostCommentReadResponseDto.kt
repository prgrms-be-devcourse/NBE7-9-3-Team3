package org.example.backend.domain.postcomment.dto;

import org.example.backend.domain.post.entity.Post.BoardType;
import org.example.backend.domain.post.entity.Post.Category;

public record MyPostCommentReadResponseDto(
    Long id,
    Long postId,
    String postTitle,
    String content,
    BoardType boardType,
    Category category
) {}
