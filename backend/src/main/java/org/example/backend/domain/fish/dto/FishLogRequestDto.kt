package org.example.backend.domain.fish.dto

import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime

/**
 * 물고기 로그 생성/수정 요청 DTO
 * 
 * @param fishId 물고기 ID (PathVariable에서 받아 컨트롤러에서 설정됨, DTO에서는 선택적)
 * @param status 상태 (필수)
 * @param logDate 기록 일시 (선택적, null일 경우 현재 시간으로 설정)
 */
data class FishLogRequestDto(
    var fishId: Long? = null,
    @field:NotNull(message = "status는 필수입니다")
    var status: String? = null,
    var logDate: LocalDateTime? = null
)
