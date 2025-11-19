package org.example.backend.domain.postcomment.dto

import jakarta.validation.constraints.NotBlank

data class PostCommentCreateRequestDto(

    @field:NotBlank(message = "댓글 내용은 필수입니다.")
    val content: String,

    val postId: Long
)
