package org.example.backend.domain.post.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.example.backend.domain.post.entity.Post

@JvmRecord
data class PostWriteRequestDto(

    @field:NotBlank(message = "제목은 필수입니다.")
    val title: String,

    @field:NotBlank(message = "내용은 필수입니다.")
    val content: String,

    @JvmField
    @field:NotNull(message = "게시판 타입은 필수입니다.")
    val boardType: Post.BoardType,

    @JvmField
    val imageUrls: MutableList<String> = mutableListOf(),

    val category: Post.Category? = null

)
