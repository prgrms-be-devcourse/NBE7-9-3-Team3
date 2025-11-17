package org.example.backend.domain.fish.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class FishRequestDto(
    @field:NotBlank
    @field:Size(min = 1, max = 50)
    val species: String,

    @field:NotBlank
    @field:Size(min = 1, max = 50)
    val name: String
)
