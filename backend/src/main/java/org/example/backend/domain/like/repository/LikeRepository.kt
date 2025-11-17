package org.example.backend.domain.like.repository

import org.example.backend.domain.like.entity.Like
import org.example.backend.domain.member.entity.Member
import org.example.backend.domain.post.entity.Post
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface LikeRepository : JpaRepository<Like, Long> {
    fun findByMemberAndPost(member: Member, post: Post): Like?

    fun findAllByMember(member: Member): List<Like>

    fun existsByMemberAndPost(member: Member, post: Post): Boolean

    @Query("SELECT l.post.id FROM Like l WHERE l.member = :member")
    fun findPostIdsByMember(@Param("member") member: Member): List<Long>
}
