package org.example.backend.domain.post.dto

@JvmRecord
data class PostListResponseDto(
    val posts: List<PostReadResponseDto>,
    val totalCount: Int
)
