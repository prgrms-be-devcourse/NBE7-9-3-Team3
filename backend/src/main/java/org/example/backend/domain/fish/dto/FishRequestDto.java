package org.example.backend.domain.fish.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record FishRequestDto(
    @NotNull
    @Size(min = 1, max = 50)
    String species,

    @NotNull
    @Size(min = 1, max = 50)
    String name
) {

}
