package org.example.backend.domain.postcomment.repository

import org.example.backend.domain.postcomment.entity.PostComment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface PostCommentRepository : JpaRepository<PostComment, Long> {
    @Query("SELECT c FROM PostComment c JOIN FETCH c.post WHERE c.author.memberId = :memberId")
    fun findByAuthor_MemberIdWithPost(@Param("memberId") memberId: Long): List<PostComment>

    @Query("SELECT c FROM PostComment c JOIN FETCH c.author WHERE c.id = :id")
    fun findByIdWithAuthor(@Param("id") id: Long): PostComment?

    @Query(
        ("SELECT c FROM PostComment c " +
                "JOIN FETCH c.author " +
                "WHERE c.post.id = :postId " +
                "ORDER BY c.createDate DESC, c.id DESC")
    )
    fun findByPostIdWithAuthor(@Param("postId") postId: Long): List<PostComment>
}
