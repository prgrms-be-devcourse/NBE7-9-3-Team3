package org.example.backend.domain.postcomment.dto

import org.example.backend.domain.postcomment.entity.PostComment

@JvmRecord
data class PostCommentResponseDto(
    val id: Long,
    val postId: Long,
    val content: String,
    val nickname: String
) {
    constructor(postComment: PostComment) : this(
        postComment.id,
        postComment.post.id,
        postComment.content,
        postComment.author.nickname
    )
}
