package org.example.backend.domain.aquarium.dto;

import lombok.*;
import org.example.backend.domain.aquarium.entity.AquariumLog;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AquariumLogResponseDto {

    private Long logId;
    private Long aquariumId;
    private Double temperature;
    private Double ph;
    private LocalDateTime logDate;

    // Entity -> DTO 변환
    public static AquariumLogResponseDto from(AquariumLog aquariumLog) {
        return AquariumLogResponseDto.builder()
                .logId(aquariumLog.getLogId())
                .aquariumId(aquariumLog.getAquarium().getId())
                .temperature(aquariumLog.getTemperature())
                .ph(aquariumLog.getPh())
                .logDate(aquariumLog.getLogDate())
                .build();
    }
}
