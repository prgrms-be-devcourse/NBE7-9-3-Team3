package org.example.backend.domain.member.dto;

public record MemberLoginResponseDto(
    Long memberId,
    String email,
    String nickname,
    String profileImage
) {}


