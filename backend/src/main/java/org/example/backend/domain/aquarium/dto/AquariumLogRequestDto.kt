package org.example.backend.domain.aquarium.dto

import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime

/**
 * 어항 로그 생성/수정 요청 DTO
 * 
 * @param aquariumId 어항 ID (PathVariable에서 받아 컨트롤러에서 설정됨, DTO에서는 선택적)
 * @param temperature 온도 (필수)
 * @param ph pH 값 (필수)
 * @param logDate 기록 일시 (필수)
 */
data class AquariumLogRequestDto(
    var aquariumId: Long? = null,
    @field:NotNull(message = "temperature는 필수입니다")
    var temperature: Double? = null,
    @field:NotNull(message = "ph는 필수입니다")
    var ph: Double? = null,
    @field:NotNull(message = "logDate는 필수입니다")
    var logDate: LocalDateTime? = null
)
