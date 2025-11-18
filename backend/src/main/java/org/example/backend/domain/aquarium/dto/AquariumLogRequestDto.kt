package org.example.backend.domain.aquarium.dto

import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime

/**
 * 어항 로그 생성/수정 요청 DTO
 * 
 * @param aquariumId 어항 ID (PathVariable에서 받아 설정됨)
 * @param temperature 온도
 * @param ph pH 값
 * @param logDate 기록 일시
 */
data class AquariumLogRequestDto(
    @field:NotNull
    var aquariumId: Long,
    @field:NotNull
    var temperature: Double,
    @field:NotNull
    var ph: Double,
    @field:NotNull
    var logDate: LocalDateTime
)
