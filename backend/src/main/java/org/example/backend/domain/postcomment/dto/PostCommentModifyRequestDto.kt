package org.example.backend.domain.postcomment.dto

@JvmRecord
data class PostCommentModifyRequestDto(
    @JvmField
    val content: String
)
