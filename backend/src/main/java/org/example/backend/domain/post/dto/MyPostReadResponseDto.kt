package org.example.backend.domain.post.dto

import org.example.backend.domain.post.entity.Post.Displaying

@JvmRecord
data class MyPostReadResponseDto(
    val id: Long,
    val title: String,
    val displaying: Displaying
)

