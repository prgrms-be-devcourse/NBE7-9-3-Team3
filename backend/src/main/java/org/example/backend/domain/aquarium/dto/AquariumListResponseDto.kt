package org.example.backend.domain.aquarium.dto

import org.example.backend.domain.aquarium.entity.Aquarium
import java.time.LocalDateTime

// TODO: @JvmRecord 삭제
@JvmRecord
data class AquariumListResponseDto(
    val aquariumId: Long,
    val aquariumName: String,
    val createDate: LocalDateTime
) {
    constructor(aquarium: Aquarium) : this(
        aquarium.id,
        aquarium.name,
        aquarium.createDate
    )
}
