package org.example.backend.domain.follow.dto

data class FollowListResponseDto(
    val users: List<FollowResponseDto>,
    val totalCount: Long
)
