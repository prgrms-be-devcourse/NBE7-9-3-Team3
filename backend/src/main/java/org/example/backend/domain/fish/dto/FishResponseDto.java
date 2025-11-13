package org.example.backend.domain.fish.dto;

import org.example.backend.domain.fish.entity.Fish;

public record FishResponseDto(
    Long fishId,
    String fishSpecies,
    String fishName
) {

  public FishResponseDto(Fish fish) {
    this(
        fish.getId(),
        fish.getSpecies(),
        fish.getName()
    );
  }
}
