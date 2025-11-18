package org.example.backend.domain.post.repository

import org.example.backend.domain.post.entity.Post
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface PostRepository : JpaRepository<Post, Long>,PostRepositoryCustom  {

    fun findByBoardType(boardType: Post.BoardType): List<Post>

    @Query(
        ("SELECT p FROM Post p " +
                "JOIN FETCH p.author " +
                "LEFT JOIN FETCH p.images " +
                "WHERE p.id = :id")
    )
    fun findByIdWithAuthorAndImages(
        @Param("id") id: Long
    ): Post?

    @Query(
        ("SELECT p FROM Post p " +
                "JOIN FETCH p.author " +
                "WHERE p.boardType = :boardType AND p.author.memberId = :id")
    )
    fun findMyPostsWithAuthor(
        @Param("boardType") boardType: Post.BoardType,
        @Param("id") id: Long
    ): List<Post>
}
