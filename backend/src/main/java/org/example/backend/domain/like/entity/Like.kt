package org.example.backend.domain.like.entity

import jakarta.persistence.*
import lombok.Getter
import lombok.NoArgsConstructor
import org.example.backend.domain.member.entity.Member
import org.example.backend.domain.post.entity.Post
import org.example.backend.global.jpa.entity.BaseEntity

@Entity
@NoArgsConstructor
@Getter
@Table(name = "likes",
    uniqueConstraints = [UniqueConstraint(columnNames = ["post_id", "user_id"])])
class Like(

    @field:JoinColumn(name = "user_id", nullable = false)
    @field:ManyToOne(fetch = FetchType.LAZY)
    var member: Member,

    @field:JoinColumn(name = "post_id", nullable = false)
    @field:ManyToOne(fetch = FetchType.LAZY)
    var post: Post
) : BaseEntity()
