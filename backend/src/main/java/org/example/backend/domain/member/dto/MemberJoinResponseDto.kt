package org.example.backend.domain.member.dto;

import java.time.LocalDateTime;
import org.example.backend.domain.member.entity.Member;

public record MemberJoinResponseDto(
    Long memberId,
    String email,
    LocalDateTime createDate,
    String nickname,
    String profileImage
) {
    public MemberJoinResponseDto(Member member){
        this(
            member.getMemberId(),
            member.getEmail(),
            member.getCreateDate(),
            member.getNickname(),
            member.getProfileImage()
        );
    }
    public static MemberJoinResponseDto from(Member member){
        return new MemberJoinResponseDto(member);
    }

}
