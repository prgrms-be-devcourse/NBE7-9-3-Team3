package org.example.backend.domain.fish.controller

import jakarta.validation.Valid
import org.example.backend.domain.fish.dto.FishLogRequestDto
import org.example.backend.domain.fish.dto.FishLogResponseDto
import org.example.backend.domain.fish.service.FishLogService
import org.example.backend.global.response.ApiResponse
import org.springframework.web.bind.annotation.*

/**
 * 물고기 로그 컨트롤러
 * 물고기 로그 관련 REST API 엔드포인트를 제공
 */
@RestController
@RequestMapping("/api/fish/{fishId}/fishLog")
class FishLogController(
    private val fishLogService: FishLogService
) : FishLogControllerSpec {

    @PostMapping
    override fun createLog(
        @PathVariable fishId: Long,
        @Valid @RequestBody requestDto: FishLogRequestDto
    ): ApiResponse<FishLogResponseDto> {
        requestDto.fishId = fishId
        val responseDto = fishLogService.createLog(requestDto)
        return ApiResponse.ok("물고기 기록이 생성되었습니다.", responseDto)
    }

    @GetMapping
    override fun getLogsByFishId(
        @PathVariable fishId: Long
    ): ApiResponse<List<FishLogResponseDto>> {
        val logs = fishLogService.getLogsByFishId(fishId)
        return ApiResponse.ok("물고기 기록 목록이 조회되었습니다.", logs)
    }

    @PutMapping("/{logId}")
    override fun updateLog(
        @PathVariable fishId: Long,
        @PathVariable logId: Long,
        @Valid @RequestBody requestDto: FishLogRequestDto
    ): ApiResponse<FishLogResponseDto> {
        requestDto.fishId = fishId
        val responseDto = fishLogService.updateLog(logId, requestDto)
        return ApiResponse.ok("물고기 기록이 수정되었습니다.", responseDto)
    }

    @DeleteMapping("/{logId}")
    override fun deleteLog(
        @PathVariable fishId: Long,
        @PathVariable logId: Long
    ): ApiResponse<Void> {
        fishLogService.deleteLog(logId)
        return ApiResponse.ok("물고기 기록이 삭제되었습니다.")
    }
}
