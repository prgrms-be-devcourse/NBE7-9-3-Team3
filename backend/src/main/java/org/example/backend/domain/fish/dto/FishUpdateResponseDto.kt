package org.example.backend.domain.fish.dto

import org.example.backend.domain.fish.entity.Fish
import java.time.LocalDateTime

data class FishUpdateResponseDto(
    val fishId: Long,
    val fishSpecies: String,
    val fishName: String,
    val createdDate: LocalDateTime?,
    val modifiedDate: LocalDateTime?
) {
    constructor(fish: Fish) : this(
        fish.id,
        fish.species,
        fish.name,
        fish.createDate,
        fish.modifyDate
    )
}
