package org.example.backend.domain.post.dto

import org.example.backend.domain.post.entity.Post
import java.time.LocalDateTime

@JvmRecord
data class PostReadResponseDto(
    val id: Long,
    val title: String,
    val content: String,
    val nickname: String,
    val createDate: LocalDateTime,
    val images: List<String>,
    val likeCount: Int,
    val liked: Boolean,
    val following: Boolean,
    val authorId: Long?,
    val category: Post.Category?,   //자랑게시판 글은 카테고리 Null임
    val isMine: Boolean

)
