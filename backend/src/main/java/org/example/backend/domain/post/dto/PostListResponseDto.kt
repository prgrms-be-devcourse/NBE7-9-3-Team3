package org.example.backend.domain.post.dto

data class PostListResponseDto(
    val posts: List<PostReadResponseDto>,
    val totalCount: Int
)
