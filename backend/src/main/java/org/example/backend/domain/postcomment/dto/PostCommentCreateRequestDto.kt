package org.example.backend.domain.postcomment.dto

import jakarta.validation.constraints.NotBlank

@JvmRecord
data class PostCommentCreateRequestDto(

    @JvmField
    @field:NotBlank(message = "댓글 내용은 필수입니다.")
    val content: String,

    @JvmField
    val postId: Long
)
