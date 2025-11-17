package org.example.backend.domain.post.dto

import jakarta.validation.constraints.NotBlank

@JvmRecord
data class PostModifyRequestDto(

    @JvmField
    @field:NotBlank(message = "제목은 필수입니다.")
    val title: String,

    @JvmField
    @field:NotBlank(message = "내용은 필수입니다.")
    val content: String,

    @JvmField
    val imageUrls: List<String>
)
