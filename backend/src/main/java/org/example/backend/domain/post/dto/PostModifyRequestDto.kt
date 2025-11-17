package org.example.backend.domain.post.dto

import jakarta.validation.constraints.NotBlank

data class PostModifyRequestDto(

    @field:NotBlank(message = "제목은 필수입니다.")
    val title: String,

    @field:NotBlank(message = "내용은 필수입니다.")
    val content: String,

    val imageUrls: List<String>
)
