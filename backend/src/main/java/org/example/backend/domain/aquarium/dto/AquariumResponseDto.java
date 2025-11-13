package org.example.backend.domain.aquarium.dto;

import java.time.LocalDateTime;
import org.example.backend.domain.aquarium.entity.Aquarium;

public record AquariumResponseDto(
    Long aquariumId,
    String aquariumName,
    LocalDateTime createDate,
    int notifyCycleDate,
    LocalDateTime lastNotifyDate,
    LocalDateTime nextNotifyDate
) {
  public AquariumResponseDto(Aquarium aquarium) {
    this (
        aquarium.getId(),
        aquarium.getName(),
        aquarium.getCreateDate(),
        aquarium.getCycleDate(),
        aquarium.getLastDate(),
        aquarium.getNextDate()
    );
  }
}
