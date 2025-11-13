package org.example.backend.domain.aquarium.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.backend.domain.aquarium.dto.AquariumLogRequestDto;
import org.example.backend.domain.aquarium.dto.AquariumLogResponseDto;
import org.example.backend.global.response.ApiResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "AquariumLog", description = "어항 기록 관리 API")
public interface AquariumLogControllerSpec {

    @Operation(summary = "어항 기록 생성", description = "특정 어항 관리를 위한 기록을 생성합니다.")
    ApiResponse<AquariumLogResponseDto> createLog(
            @PathVariable Long aquariumId,
            @RequestBody AquariumLogRequestDto requestDto
    );

    @Operation(summary = "어항 기록 목록 조회", description = "특정 어항의 모든 기록을 조회합니다.")
    ApiResponse<List<AquariumLogResponseDto>> getLogsByAquariumId(@PathVariable Long aquariumId);

    @Operation(summary = "어항 기록 수정", description = "특정 어항의 특정 기록을 수정합니다.")
    ApiResponse<AquariumLogResponseDto> updateLog(
            @PathVariable Long aquariumId,
            @PathVariable Long logId,
            @RequestBody AquariumLogRequestDto requestDto
    );

    @Operation(summary = "어항 기록 삭제", description = "특정 어항의 특정 기록을 삭제합니다.")
    ApiResponse<Void> deleteLog(
            @PathVariable Long aquariumId,
            @PathVariable Long logId
    );
}
