package org.example.backend.domain.member.dto

data class MemberLoginResponseDto(
    val memberId: Long?,
    val email: String,
    val nickname: String,
    val profileImage: String?
)


