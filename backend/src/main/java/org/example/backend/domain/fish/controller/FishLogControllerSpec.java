package org.example.backend.domain.fish.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.backend.domain.fish.dto.FishLogRequestDto;
import org.example.backend.domain.fish.dto.FishLogResponseDto;
import org.example.backend.global.response.ApiResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "FishLog", description = "물고기 기록 관리 API")
public interface FishLogControllerSpec {

    @Operation(summary = "물고기 기록 생성", description = "특정 물고기 관리를 위한 기록을 생성합니다.")
    ApiResponse<FishLogResponseDto> createLog(
            @PathVariable Long fishId,
            @RequestBody FishLogRequestDto requestDto
    );

    @Operation(summary = "물고기 기록 목록 조회", description = "특정 물고기의 모든 기록을 조회합니다.")
    ApiResponse<List<FishLogResponseDto>> getLogsByFishId(@PathVariable Long fishId);

    @Operation(summary = "물고기 기록 수정", description = "특정 물고기의 특정 기록을 수정합니다.")
    ApiResponse<FishLogResponseDto> updateLog(
            @PathVariable Long fishId,
            @PathVariable Long logId,
            @RequestBody FishLogRequestDto requestDto
    );

    @Operation(summary = "물고기 기록 삭제", description = "특정 물고기의 특정 기록을 삭제합니다.")
    ApiResponse<Void> deleteLog(
            @PathVariable Long fishId,
            @PathVariable Long logId
    );
}
