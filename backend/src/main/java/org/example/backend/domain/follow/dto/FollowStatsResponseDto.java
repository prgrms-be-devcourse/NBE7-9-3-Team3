package org.example.backend.domain.follow.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FollowStatsResponseDto {
    private long followerCount; // 나를 팔로우하는 사람 수
    private long followingCount; // 내가 팔로우하는 사람 수

    @Builder
    public FollowStatsResponseDto(long followerCount, long followingCount) {
        this.followerCount = followerCount;
        this.followingCount = followingCount;
    }
}
