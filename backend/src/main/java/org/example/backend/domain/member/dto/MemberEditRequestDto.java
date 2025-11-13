package org.example.backend.domain.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MemberEditRequestDto(
    @NotBlank
    @Email
    String email,

    @NotBlank
    @Size(min = 8, max = 20)
    String currentPassword, // 현재 비밀번호 (필수)

    @NotBlank
    @Size(min = 8, max = 20)
    String newPassword, // 새로운 비밀번호

    @NotBlank
    String nickname,

    String profileImageUrl
) {}
