package org.example.backend.domain.aquarium.dto

import org.example.backend.domain.aquarium.entity.AquariumLog
import org.example.backend.domain.aquarium.util.getIdSafely
import java.time.LocalDateTime

/**
 * 어항 로그 응답 DTO
 * 
 * @param logId 로그 ID (프라이머리 키, 항상 값이 있음)
 * @param aquariumId 어항 ID (항상 값이 있음)
 * @param temperature 온도
 * @param ph pH 값
 * @param logDate 기록 일시 (항상 값이 있음)
 */
data class AquariumLogResponseDto(
    val logId: Long,
    val aquariumId: Long,
    val temperature: Double?,
    val ph: Double?,
    val logDate: LocalDateTime
) {
    companion object {
        /**
         * Entity를 DTO로 변환
         * 저장된 엔티티만 DTO로 변환 가능 (ID가 있어야 함)
         * @param aquariumLog 어항 로그 엔티티
         * @return 어항 로그 응답 DTO
         */
        @JvmStatic
        fun from(aquariumLog: AquariumLog): AquariumLogResponseDto {
            // 저장된 엔티티는 항상 ID를 가지므로 0 체크 (저장 전 0, 저장 후 항상 0보다 큰 값)
            val logId = aquariumLog.logId
            if (logId == 0L) {
                throw IllegalStateException("logId is 0. Entity must be persisted before converting to DTO.")
            }
            
            // aquarium은 lateinit var이고 nullable = false이므로 항상 값이 있음
            val aquariumId = aquariumLog.aquarium.getIdSafely()
                ?: throw IllegalStateException("aquariumId is null. Aquarium must be set.")
            
            return AquariumLogResponseDto(
                logId = logId,
                aquariumId = aquariumId,
                temperature = aquariumLog.temperature,
                ph = aquariumLog.ph,
                logDate = aquariumLog.logDate
            )
        }
    }
}
