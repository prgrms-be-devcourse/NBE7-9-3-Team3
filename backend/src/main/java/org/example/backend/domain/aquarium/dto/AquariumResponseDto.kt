package org.example.backend.domain.aquarium.dto

import org.example.backend.domain.aquarium.entity.Aquarium
import java.time.LocalDateTime

@JvmRecord
data class AquariumResponseDto(
    val aquariumId: Long?,
    val aquariumName: String?,
    val createDate: LocalDateTime?,
    val notifyCycleDate: Int,
    val lastNotifyDate: LocalDateTime?,
    val nextNotifyDate: LocalDateTime?
) {
    constructor(aquarium: Aquarium) : this(
        aquarium.getId(),
        aquarium.name,
        aquarium.getCreateDate(),
        aquarium.cycleDate,
        aquarium.lastDate,
        aquarium.nextDate
    )
}
