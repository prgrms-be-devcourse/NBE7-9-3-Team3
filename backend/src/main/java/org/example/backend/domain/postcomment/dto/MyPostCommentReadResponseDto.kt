package org.example.backend.domain.postcomment.dto

import org.example.backend.domain.post.entity.Post

@JvmRecord
data class MyPostCommentReadResponseDto(
    val id: Long,
    val postId: Long,
    val postTitle: String,
    val content: String,
    val boardType: Post.BoardType,
    val category: Post.Category?
)
