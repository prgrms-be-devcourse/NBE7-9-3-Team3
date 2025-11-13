package org.example.backend.domain.post.dto;

import java.util.List;

public record PostListResponseDto(
    List<PostReadResponseDto> posts,
    int totalCount
) {}
