package org.example.backend.domain.aquarium.dto

import org.example.backend.domain.aquarium.entity.Aquarium
import java.time.LocalDateTime

data class AquariumResponseDto(
    val aquariumId: Long,
    val aquariumName: String,
    val createDate: LocalDateTime,
    val notifyCycleDate: Int,
    val lastNotifyDate: LocalDateTime?,
    val nextNotifyDate: LocalDateTime?
) {
    constructor(aquarium: Aquarium) : this(
        aquarium.id,
        aquarium.name,
        aquarium.createDate,
        aquarium.cycleDate,
        aquarium.lastDate,
        aquarium.nextDate
    )
}
