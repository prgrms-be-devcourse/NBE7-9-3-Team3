package org.example.backend.domain.aquarium.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AquariumLogRequestDto {

    private Long aquariumId;
    private Double temperature;
    private Double ph;
    private LocalDateTime logDate;
}
