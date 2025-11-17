package org.example.backend.domain.aquarium.dto

import java.time.LocalDateTime

/**
 * 어항 로그 생성/수정 요청 DTO
 * 
 * @param aquariumId 어항 ID
 * @param temperature 온도
 * @param ph pH 값
 * @param logDate 기록 일시 (null일 경우 현재 시간으로 설정)
 */
data class AquariumLogRequestDto(
    var aquariumId: Long? = null,
    var temperature: Double? = null,
    var ph: Double? = null,
    var logDate: LocalDateTime? = null
)
