package org.example.backend.domain.fish.dto

import org.example.backend.domain.fish.entity.Fish

data class FishResponseDto(
    val fishId: Long,
    val fishSpecies: String,
    val fishName: String
) {
    constructor(fish: Fish) : this(
        fish.id,
        fish.species,
        fish.name
    )
}
