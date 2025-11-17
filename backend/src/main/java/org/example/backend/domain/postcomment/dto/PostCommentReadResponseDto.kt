package org.example.backend.domain.postcomment.dto

@JvmRecord
data class PostCommentReadResponseDto(
    val id: Long,
    val content: String,
    val nickname: String,
    val isMine: Boolean
)
