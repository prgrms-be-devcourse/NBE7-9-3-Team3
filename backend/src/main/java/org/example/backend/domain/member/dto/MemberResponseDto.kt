package org.example.backend.domain.member.dto

import org.example.backend.domain.member.entity.Member

data class MemberResponseDto(
    val memberId: Long?,
    val email: String,
    val nickname: String,
    val profileImage: String?,
    val followerCount: Long?,
    val followingCount: Long?
) {
    constructor(member: Member, followerCount: Long?, followingCount: Long?) : this(
        member.memberId,
        member.email,
        member.nickname,
        member.profileImage,
        followerCount,
        followingCount
    )
}
