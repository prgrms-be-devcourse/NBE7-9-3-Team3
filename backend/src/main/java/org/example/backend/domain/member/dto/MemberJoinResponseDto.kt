package org.example.backend.domain.member.dto

import org.example.backend.domain.member.entity.Member
import java.time.LocalDateTime

data class MemberJoinResponseDto(
    val memberId: Long?,
    val email: String?,
    val createDate: LocalDateTime?,
    val nickname: String?,
    val profileImage: String?
) {
    constructor(member: Member) : this(
        member.memberId,
        member.email,
        member.createDate,
        member.nickname,
        member.profileImage
    )

    companion object {
        @JvmStatic
        fun from(member: Member): MemberJoinResponseDto {
            return MemberJoinResponseDto(member)
        }
    }
}
