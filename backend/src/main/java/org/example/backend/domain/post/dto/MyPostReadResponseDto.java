package org.example.backend.domain.post.dto;

import org.example.backend.domain.post.entity.Post.Displaying;

public record MyPostReadResponseDto(
    Long id,
    String title,
    Displaying displaying
) {

}

