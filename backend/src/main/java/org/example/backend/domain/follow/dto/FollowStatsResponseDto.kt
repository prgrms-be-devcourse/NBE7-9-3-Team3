package org.example.backend.domain.follow.dto

data class FollowStatsResponseDto(
    val followerCount: Long, // 나를 팔로우하는 사람 수
    val followingCount: Long // 내가 팔로우하는 사람 수
)
