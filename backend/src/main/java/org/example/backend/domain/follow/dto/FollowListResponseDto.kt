package org.example.backend.domain.follow.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class FollowListResponseDto {
    private List<FollowResponseDto> users;
    private long totalCount;

    @Builder
    public FollowListResponseDto(List<FollowResponseDto> users, long totalCount) {
        this.users = users;
        this.totalCount = totalCount;
    }
}
