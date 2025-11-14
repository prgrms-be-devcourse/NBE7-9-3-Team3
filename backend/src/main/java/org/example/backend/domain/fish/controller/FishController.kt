package org.example.backend.domain.fish.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import lombok.RequiredArgsConstructor
import org.example.backend.domain.fish.dto.FishRequestDto
import org.example.backend.domain.fish.dto.FishResponseDto
import org.example.backend.domain.fish.dto.FishUpdateResponseDto
import org.example.backend.domain.fish.service.FishService
import org.example.backend.global.response.ApiResponse
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/aquarium/{aquariumId}/fish")
@Tag(name = "Fish", description = "물고기 관리 API")
class FishController(
    private val fishService: FishService
) {

    // 물고기 생성
    @Operation(summary = "물고기 생성", description = "새로운 물고기를 생성합니다.")
    @PostMapping
    fun createFish(
        @PathVariable aquariumId: Long,
        @RequestBody fishRequestDto: FishRequestDto
    ): ApiResponse<FishResponseDto> {
        val responseDto = fishService.createFish(aquariumId, fishRequestDto)

        return ApiResponse.ok("물고기가 생성되었습니다.", responseDto)
    }

    // 물고기 다건 조회
    @Operation(summary = "물고기 목록 조회", description = "특정 어항의 모든 물고기를 조회합니다.")
    @GetMapping
    fun getFishes(@PathVariable aquariumId: Long): ApiResponse<List<FishResponseDto>> {
        val responseDto = fishService.findAllByAquariumId(aquariumId)

        return ApiResponse.ok("물고기들이 조회되었습니다.", responseDto)
    }

    // 물고기 수정
    @Operation(summary = "물고기 수정", description = "특정 물고기의 종과 이름을 수정합니다.")
    @PutMapping("/{fishId}")
    fun updateFish(
        @PathVariable aquariumId: Long, @PathVariable fishId: Long,
        @RequestBody fishRequestDto: FishRequestDto
    ): ApiResponse<FishUpdateResponseDto> {
        val responseDto = fishService.updateFish(aquariumId, fishId, fishRequestDto)

        return ApiResponse.ok("물고기 종과 이름이 수정되었습니다.", responseDto)
    }

    // 물고기 삭제
    @Operation(summary = "물고기 삭제", description = "특정 물고기를 삭제합니다.")
    @DeleteMapping("/{fishId}")
    fun deleteFish(@PathVariable aquariumId: Long, @PathVariable fishId: Long): ApiResponse<Void> {
        fishService.deleteFish(aquariumId, fishId)

        return ApiResponse.ok("물고기가 삭제되었습니다.")
    }
}
