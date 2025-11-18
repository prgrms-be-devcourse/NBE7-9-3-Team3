package org.example.backend.domain.member.dto

data class MemberSearchResponseDto(
    val memberId: Long?,
    val nickname: String,
    val profileImage: String?,
    val isFollowing: Boolean = false
)
