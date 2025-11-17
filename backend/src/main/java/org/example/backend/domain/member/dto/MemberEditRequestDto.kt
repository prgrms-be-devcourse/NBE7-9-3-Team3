package org.example.backend.domain.member.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class MemberEditRequestDto(
    @field:NotBlank @field:Email val email: String?,
    @field:NotBlank @field:Size(min = 8, max = 20) val currentPassword: String?,  // 현재 비밀번호 (필수)
    @field:NotBlank @field:Size(min = 8, max = 20) val newPassword: String?,  // 새로운 비밀번호
    @field:NotBlank val nickname: String?,
    val profileImageUrl: String?
)
