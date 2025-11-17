package org.example.backend.domain.member.dto;

import org.example.backend.domain.member.entity.Member;

public record MemberResponseDto(
    Long memberId,
    String email,
    String nickname,
    String profileImage,
    Long followerCount,
    Long followingCount
) {
    public MemberResponseDto(Member member, Long followerCount, Long followingCount){
        this(
            member.getMemberId(),
            member.getEmail(),
            member.getNickname(),
            member.getProfileImage(),
            followerCount,
            followingCount
        );
    }
}
