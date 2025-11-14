package org.example.backend.domain.aquarium.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class AquariumRequestDto(
    @field:NotBlank
    @field:Size(min = 1, max = 50)
    val aquariumName: String
)