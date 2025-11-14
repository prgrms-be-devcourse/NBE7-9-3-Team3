package org.example.backend.domain.aquarium.dto

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

@JvmRecord
data class AquariumRequestDto(
    @JvmField val aquariumName: @NotNull @Size(min = 1, max = 50) String
)