package org.example.backend.domain.member.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberSearchResponseDto {
    private Long memberId;
    private String nickname;
    private String profileImage;
    private boolean isFollowing;
}
