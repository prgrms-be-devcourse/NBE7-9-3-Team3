package org.example.backend.domain.fish.dto

import org.example.backend.domain.fish.entity.FishLog
import java.time.LocalDateTime

/**
 * 물고기 로그 응답 DTO
 * 
 * @param logId 로그 ID (프라이머리 키, 항상 값이 있음)
 * @param aquariumId 어항 ID (항상 값이 있음)
 * @param fishId 물고기 ID (항상 값이 있음)
 * @param status 상태 (항상 값이 있음)
 * @param logDate 기록 일시 (항상 값이 있음)
 */
data class FishLogResponseDto(
    val logId: Long,
    val aquariumId: Long,
    val fishId: Long,
    val status: String,
    val logDate: LocalDateTime
) {
    companion object {
        /**
         * Entity를 DTO로 변환
         * 저장된 엔티티만 DTO로 변환 가능 (ID가 있어야 함)
         * @param fishLog 물고기 로그 엔티티
         * @return 물고기 로그 응답 DTO
         */
        @JvmStatic
        fun from(fishLog: FishLog): FishLogResponseDto {
            // 저장된 엔티티는 항상 ID를 가지므로 0 체크 (저장 전 0, 저장 후 항상 0보다 큰 값)
            val logId = fishLog.logId
            if (logId == 0L) {
                throw IllegalStateException("logId is 0. Entity must be persisted before converting to DTO.")
            }
            
            // fish는 lateinit var이고 nullable = false이므로 항상 값이 있음
            val fish = fishLog.fish
            val fishId = fish.id
            if (fishId == 0L) {
                throw IllegalStateException("fishId is 0. Fish must be persisted.")
            }
            
            // aquarium도 항상 값이 있음
            val aquariumId = fish.aquarium.id
            if (aquariumId == 0L) {
                throw IllegalStateException("aquariumId is 0. Aquarium must be persisted.")
            }
            
            return FishLogResponseDto(
                logId = logId,
                aquariumId = aquariumId,
                fishId = fishId,
                status = fishLog.status,
                logDate = fishLog.logDate
            )
        }
    }
}
