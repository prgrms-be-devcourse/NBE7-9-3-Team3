package org.example.backend.domain.member.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MemberSearchListResponseDto {
    private List<MemberSearchResponseDto> members;
}
