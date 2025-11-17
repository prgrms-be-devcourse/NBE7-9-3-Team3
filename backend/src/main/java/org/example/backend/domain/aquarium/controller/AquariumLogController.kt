package org.example.backend.domain.aquarium.controller

import org.example.backend.domain.aquarium.dto.AquariumLogRequestDto
import org.example.backend.domain.aquarium.dto.AquariumLogResponseDto
import org.example.backend.domain.aquarium.service.AquariumLogService
import org.example.backend.global.response.ApiResponse
import org.springframework.web.bind.annotation.*

/**
 * 어항 로그 컨트롤러
 * 어항 로그 관련 REST API 엔드포인트를 제공
 */
@RestController
@RequestMapping("/api/aquarium/{aquariumId}/aquariumLog")
class AquariumLogController(
    private val aquariumLogService: AquariumLogService
) : AquariumLogControllerSpec {

    @PostMapping
    override fun createLog(
        @PathVariable aquariumId: Long,
        @RequestBody requestDto: AquariumLogRequestDto
    ): ApiResponse<AquariumLogResponseDto> {
        requestDto.aquariumId = aquariumId
        val responseDto = aquariumLogService.createLog(requestDto)
        return ApiResponse.ok("어항 기록이 생성되었습니다.", responseDto)
    }

    @GetMapping
    override fun getLogsByAquariumId(
        @PathVariable aquariumId: Long
    ): ApiResponse<List<AquariumLogResponseDto>> {
        val logs = aquariumLogService.getLogsByAquariumId(aquariumId)
        return ApiResponse.ok("어항 기록 목록이 조회되었습니다.", logs)
    }

    @PutMapping("/{logId}")
    override fun updateLog(
        @PathVariable aquariumId: Long,
        @PathVariable logId: Long,
        @RequestBody requestDto: AquariumLogRequestDto
    ): ApiResponse<AquariumLogResponseDto> {
        requestDto.aquariumId = aquariumId
        val responseDto = aquariumLogService.updateLog(logId, requestDto)
        return ApiResponse.ok("어항 기록이 수정되었습니다.", responseDto)
    }

    @DeleteMapping("/{logId}")
    override fun deleteLog(
        @PathVariable aquariumId: Long,
        @PathVariable logId: Long
    ): ApiResponse<Void> {
        aquariumLogService.deleteLog(logId)
        return ApiResponse.ok("어항 기록이 삭제되었습니다.")
    }
}
