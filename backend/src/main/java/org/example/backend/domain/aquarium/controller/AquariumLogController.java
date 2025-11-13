package org.example.backend.domain.aquarium.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.domain.aquarium.dto.AquariumLogRequestDto;
import org.example.backend.domain.aquarium.dto.AquariumLogResponseDto;
import org.example.backend.domain.aquarium.service.AquariumLogService;
import org.example.backend.global.response.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/aquarium/{aquariumId}/aquariumLog")
@RequiredArgsConstructor
public class AquariumLogController implements AquariumLogControllerSpec {

    private final AquariumLogService aquariumLogService;

    @Override
    @PostMapping
    public ApiResponse<AquariumLogResponseDto> createLog(
            @PathVariable Long aquariumId,
            @RequestBody AquariumLogRequestDto requestDto) {
        // aquariumId를 requestDto에 설정
        requestDto.setAquariumId(aquariumId);
        AquariumLogResponseDto responseDto = aquariumLogService.createLog(requestDto);
        return ApiResponse.ok("어항 기록이 생성되었습니다.", responseDto);
    }

    @Override
    @GetMapping
    public ApiResponse<List<AquariumLogResponseDto>> getLogsByAquariumId(@PathVariable Long aquariumId) {
        List<AquariumLogResponseDto> logs = aquariumLogService.getLogsByAquariumId(aquariumId);
        return ApiResponse.ok("어항 기록 목록이 조회되었습니다.", logs);
    }

    @Override
    @PutMapping("/{logId}")
    public ApiResponse<AquariumLogResponseDto> updateLog(
            @PathVariable Long aquariumId,
            @PathVariable Long logId,
            @RequestBody AquariumLogRequestDto requestDto) {
        // aquariumId를 requestDto에 설정
        requestDto.setAquariumId(aquariumId);
        AquariumLogResponseDto responseDto = aquariumLogService.updateLog(logId, requestDto);
        return ApiResponse.ok("어항 기록이 수정되었습니다.", responseDto);
    }

    @Override
    @DeleteMapping("/{logId}")
    public ApiResponse<Void> deleteLog(
            @PathVariable Long aquariumId,
            @PathVariable Long logId) {
        aquariumLogService.deleteLog(logId);
        return ApiResponse.ok("어항 기록이 삭제되었습니다.");
    }
}
