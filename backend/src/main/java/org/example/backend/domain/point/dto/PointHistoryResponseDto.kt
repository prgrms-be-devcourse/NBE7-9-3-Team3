package org.example.backend.domain.point.dto

import org.example.backend.domain.point.entity.Point
import org.example.backend.domain.point.entity.TransactionType
import java.time.LocalDateTime

data class PointHistoryResponseDto(
    val type: TransactionType?,
    val date: LocalDateTime?,
    val points: Long?,
    val afterPoint: Long?
) {
    companion object {
        @JvmStatic
        fun from(point: Point): PointHistoryResponseDto {
            return PointHistoryResponseDto(
                point.getType(),
                point.getCreateDate(),
                point.getPoints(),
                point.getAfterPoint()
            )
        }
    }
}
