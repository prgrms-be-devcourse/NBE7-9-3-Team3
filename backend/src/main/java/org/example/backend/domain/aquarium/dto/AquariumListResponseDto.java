package org.example.backend.domain.aquarium.dto;

import java.time.LocalDateTime;
import org.example.backend.domain.aquarium.entity.Aquarium;

public record AquariumListResponseDto(
    Long aquariumId,
    String aquariumName,
    LocalDateTime createDate
) {

  public AquariumListResponseDto(Aquarium aquarium) {
    this(
        aquarium.getId(),
        aquarium.getName(),
        aquarium.getCreateDate()
    );
  }
}
