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
         * Java 코드에서도 사용할 수 있도록 @JvmStatic 추가 (임시 코드)
         * @param aquariumLog 어항 로그 엔티티
         * @return 어항 로그 응답 DTO
         */
        @JvmStatic
        fun from(aquariumLog: AquariumLog): AquariumLogResponseDto {
            // Java 클래스의 getId() 메서드를 확장 함수로 호출 (임시 코드)
            // Aquarium이 Kotlin으로 전환되면 aquarium.id로 직접 접근 가능
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
