package org.example.backend.domain.aquarium.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.example.backend.domain.aquarium.dto.AquariumListResponseDto
import org.example.backend.domain.aquarium.dto.AquariumRequestDto
import org.example.backend.domain.aquarium.dto.AquariumResponseDto
import org.example.backend.domain.aquarium.dto.AquariumScheduleRequestDto
import org.example.backend.global.response.ApiResponse
import org.example.backend.global.security.CustomUserDetails
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody

@Tag(name = "Aquarium", description = "어항 관리 API")
interface AquariumControllerSpec {
    @Operation(summary = "어항 생성", description = "새로운 어항을 생성합니다.")
    fun createAquarium(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @RequestBody requestDto: AquariumRequestDto
    ): ApiResponse<AquariumListResponseDto>

    @Operation(summary = "어항 목록 조회", description = "로그인한 회원의 모든 어항을 조회합니다.")
    fun getAquariums(
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ApiResponse<List<AquariumResponseDto>>

    @Operation(summary = "어항 조회", description = "특정 어항의 상세 정보를 조회합니다.")
    fun getAquariumName(@PathVariable id: Long): ApiResponse<AquariumResponseDto>

    @Operation(summary = "어항 수정", description = "특정 어항의 이름을 수정합니다.")
    fun updateAquariumName(
        @PathVariable id: Long,
        @RequestBody requestDto: AquariumRequestDto
    ): ApiResponse<AquariumResponseDto>

    @Operation(summary = "어항 속 물고기 존재 여부 확인", description = "특정 어항의 물고기 존재 여부를 확인합니다.")
    fun checkFishInAquarium(@PathVariable id: Long): ApiResponse<Boolean>

    @Operation(summary = "물고기 이동", description = "삭제할 어항 속 물고기들을 '내가 키운 물고기' 어항으로 이동시킵니다.")
    fun moveFishToOwnedAquarium(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @PathVariable id: Long
    ): ApiResponse<String>

    @Operation(summary = "어항 삭제", description = "특정 어항을 삭제합니다.")
    fun deleteAquarium(@PathVariable id: Long): ApiResponse<Void>

    @Operation(summary = "어항 알림 스케줄 설정", description = "특정 어항의 알림 스케줄을 설정합니다.")
    fun scheduleSetting(
        @PathVariable id: Long,
        @RequestBody requestDto: AquariumScheduleRequestDto
    ): ApiResponse<AquariumResponseDto>
}
