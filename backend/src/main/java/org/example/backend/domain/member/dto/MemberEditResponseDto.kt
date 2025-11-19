package org.example.backend.domain.member.dto

import org.example.backend.domain.member.entity.Member
import java.time.LocalDateTime

data class MemberEditResponseDto(
    val memberId: Long?,
    val email: String,
    val createDate: LocalDateTime?,
    val nickname: String,
    val profileImage: String?,
    val newAccessToken: String? // 새로운 토큰 (필요한 경우에만)
) {
    constructor(member: Member, newAccessToken: String?) : this(
        member.memberId,
        member.email,
        member.createDate,
        member.nickname,
        member.profileImage,
        newAccessToken
    )

    companion object {
        fun from(member: Member, newAccessToken: String?): MemberEditResponseDto =
            MemberEditResponseDto(member, newAccessToken)
    }
}
