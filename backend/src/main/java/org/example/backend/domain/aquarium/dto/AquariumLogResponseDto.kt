package org.example.backend.domain.aquarium.dto

import org.example.backend.domain.aquarium.entity.AquariumLog
import org.example.backend.domain.aquarium.util.getIdSafely
import java.time.LocalDateTime

/**
 * 어항 로그 응답 DTO
 * 
 * @param logId 로그 ID
 * @param aquariumId 어항 ID
 * @param temperature 온도
 * @param ph pH 값
 * @param logDate 기록 일시
 */
data class AquariumLogResponseDto(
    val logId: Long?,
    val aquariumId: Long?,
    val temperature: Double?,
    val ph: Double?,
    val logDate: LocalDateTime?
) {
    companion object {
        /**
         * Entity를 DTO로 변환
         * @param aquariumLog 어항 로그 엔티티
         * @return 어항 로그 응답 DTO
         */
        @JvmStatic
        fun from(aquariumLog: AquariumLog): AquariumLogResponseDto {
            val aquariumId = aquariumLog.aquarium?.getIdSafely()
            
            return AquariumLogResponseDto(
                logId = aquariumLog.logId,
                aquariumId = aquariumId,
                temperature = aquariumLog.temperature,
                ph = aquariumLog.ph,
                logDate = aquariumLog.logDate
            )
        }
    }
}
