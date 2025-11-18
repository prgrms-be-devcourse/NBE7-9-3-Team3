package org.example.backend.domain.post.repository

import org.example.backend.domain.post.entity.Post
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface PostRepositoryCustom {
    fun searchPosts(
        boardType: Post.BoardType,
        displaying: Post.Displaying,
        keyword: String?,
        category: Post.Category?,
        authorIds: List<Long>?,
        pageable: Pageable
    ): Page<Post>
}