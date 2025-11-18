package org.example.backend.domain.follow.dto

data class FollowResponseDto(
    val memberId: Long?,
    val nickname: String,
    val profileImage: String?
)
