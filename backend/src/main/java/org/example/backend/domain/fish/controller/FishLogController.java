package org.example.backend.domain.fish.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.domain.fish.dto.FishLogRequestDto;
import org.example.backend.domain.fish.dto.FishLogResponseDto;
import org.example.backend.domain.fish.service.FishLogService;
import org.example.backend.global.response.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fish/{fishId}/fishLog")
@RequiredArgsConstructor
public class FishLogController implements FishLogControllerSpec {
    
    private final FishLogService fishLogService;
    
    @Override
    @PostMapping
    public ApiResponse<FishLogResponseDto> createLog(
            @PathVariable Long fishId,
            @RequestBody FishLogRequestDto requestDto) {
        // fishId를 requestDto에 설정
        requestDto.setFishId(fishId);
        FishLogResponseDto responseDto = fishLogService.createLog(requestDto);
        return ApiResponse.ok("물고기 기록이 생성되었습니다.", responseDto);
    }
    
    @Override
    @GetMapping
    public ApiResponse<List<FishLogResponseDto>> getLogsByFishId(
            @PathVariable Long fishId) {
        List<FishLogResponseDto> logs = fishLogService.getLogsByFishId(fishId);
        return ApiResponse.ok("물고기 기록 목록이 조회되었습니다.", logs);
    }
    
    @Override
    @PutMapping("/{logId}")
    public ApiResponse<FishLogResponseDto> updateLog(
            @PathVariable Long fishId,
            @PathVariable Long logId,
            @RequestBody FishLogRequestDto requestDto) {
        // fishId를 requestDto에 설정
        requestDto.setFishId(fishId);
        FishLogResponseDto responseDto = fishLogService.updateLog(logId, requestDto);
        return ApiResponse.ok("물고기 기록이 수정되었습니다.", responseDto);
    }
    
    @Override
    @DeleteMapping("/{logId}")
    public ApiResponse<Void> deleteLog(
            @PathVariable Long fishId,
            @PathVariable Long logId) {
        fishLogService.deleteLog(logId);
        return ApiResponse.ok("물고기 기록이 삭제되었습니다.");
    }
}
