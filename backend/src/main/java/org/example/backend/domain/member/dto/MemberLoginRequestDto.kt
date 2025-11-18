package org.example.backend.domain.member.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class MemberLoginRequestDto(
    @field:NotBlank @field:Email val email: String,
    @field:NotBlank val password: String
)


