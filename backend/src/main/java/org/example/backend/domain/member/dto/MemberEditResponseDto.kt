package org.example.backend.domain.member.dto;

import java.time.LocalDateTime;
import org.example.backend.domain.member.entity.Member;

public record MemberEditResponseDto(
    Long memberId,
    String email,
    LocalDateTime createDate,
    String nickname,
    String profileImage,
    String newAccessToken // 새로운 토큰 (필요한 경우에만)
) {
    public MemberEditResponseDto(Member member, String newAccessToken){
        this(
            member.getMemberId(),
            member.getEmail(),
            member.getCreateDate(),
            member.getNickname(),
            member.getProfileImage(),
            newAccessToken
        );
    }
    
    public static MemberEditResponseDto from(Member member, String newAccessToken){
        return new MemberEditResponseDto(member, newAccessToken);
    }
}
