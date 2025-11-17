package org.example.backend.domain.member.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class MemberJoinRequestDto(
    @field:NotBlank @field:Email val email: String,
    @field:NotBlank @field:Size(min = 8, max = 20) val password: String,
    @field:NotBlank val nickname: String,
    val profileImageUrl: String?
)
