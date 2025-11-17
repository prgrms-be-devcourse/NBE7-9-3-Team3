package org.example.backend.domain.follow.entity

import jakarta.persistence.*
import org.example.backend.domain.member.entity.Member

@Entity
@Table(
    name = "follow",
    uniqueConstraints = [UniqueConstraint(columnNames = ["follower_id", "followee_id"])]
)
class Follow(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id")
    val follower: Member,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "followee_id")
    val followee: Member,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long
)
