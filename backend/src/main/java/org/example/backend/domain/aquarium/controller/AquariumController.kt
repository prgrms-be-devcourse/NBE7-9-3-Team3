package org.example.backend.domain.aquarium.controller

import jakarta.validation.Valid
import lombok.RequiredArgsConstructor
import org.example.backend.domain.aquarium.dto.AquariumListResponseDto
import org.example.backend.domain.aquarium.dto.AquariumRequestDto
import org.example.backend.domain.aquarium.dto.AquariumResponseDto
import org.example.backend.domain.aquarium.dto.AquariumScheduleRequestDto
import org.example.backend.domain.aquarium.service.AquariumService
import org.example.backend.global.response.ApiResponse
import org.example.backend.global.security.CustomUserDetails
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/aquarium")
class AquariumController(
    private val aquariumService: AquariumService
) : AquariumControllerSpec {

    @PostMapping
    override fun createAquarium(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @RequestBody requestDto: AquariumRequestDto
    ): ApiResponse<AquariumListResponseDto> {
        val responseDto = aquariumService.create(userDetails, requestDto)

        return ApiResponse.ok("어항이 생성되었습니다.", responseDto)
    }

    @GetMapping
    override fun getAquariums(
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ApiResponse<List<AquariumResponseDto>> {
        val responseDto = aquariumService.findAllByMemberId(userDetails)

        return ApiResponse.ok("어항 목록이 조회되었습니다.", responseDto)
    }

    @GetMapping("/{id}")
    override fun getAquariumName(@PathVariable id: Long): ApiResponse<AquariumResponseDto> {
        val responseDto = aquariumService.findById(id)

        return ApiResponse.ok("어항이 조회되었습니다.", responseDto)
    }

    @PutMapping("/{id}")
    override fun updateAquariumName(
        @PathVariable id: Long,
        @RequestBody requestDto: AquariumRequestDto
    ): ApiResponse<AquariumResponseDto> {
        val responseDto = aquariumService.updateAquariumName(id, requestDto)

        return ApiResponse.ok("어항이 수정되었습니다.", responseDto)
    }

    @GetMapping("/{id}/delete")
    override fun checkFishInAquarium(@PathVariable id: Long): ApiResponse<Boolean> {
        val hasFish = aquariumService.hasFish(id)

        return ApiResponse.ok("어항의 물고기 존재 여부를 확인했습니다.", hasFish)
    }

    @PutMapping("/{id}/delete")
    override fun moveFishToOwnedAquarium(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @PathVariable id: Long
    ): ApiResponse<String> {
        aquariumService.moveFishToOwnedAquarium(userDetails, id)

        return ApiResponse.ok("물고기들이 '내가 키운 물고기' 어항으로 이동되었습니다.", "물고기 이동 완료")
    }

    @DeleteMapping("/{id}/delete")
    override fun deleteAquarium(@PathVariable id: Long): ApiResponse<Void> {
        aquariumService.delete(id)

        return ApiResponse.ok("어항이 삭제되었습니다.")
    }

    @PostMapping("/{id}/schedule")
    override fun scheduleSetting(
        @PathVariable id: Long,
        @RequestBody requestDto: @Valid AquariumScheduleRequestDto
    ): ApiResponse<AquariumResponseDto> {
        val responseDto = aquariumService.scheduleSetting(id, requestDto)

        return ApiResponse.ok("물갈이&어항세척 스케줄 알림이 설정되었습니다.", responseDto)
    }
}
