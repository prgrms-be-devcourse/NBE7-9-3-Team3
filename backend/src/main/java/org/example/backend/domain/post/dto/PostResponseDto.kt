package org.example.backend.domain.post.dto

import org.example.backend.domain.post.entity.Post

data class PostResponseDto(
    val id: Long,
    val title: String,
    val content: String,
    val imageUrls: List<String>
) {
    constructor(post: Post) : this(
        post.id,
        post.title,
        post.content,
        post.images
            .map { it.imageUrl }
    )
}
