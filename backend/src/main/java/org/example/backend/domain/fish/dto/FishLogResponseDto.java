package org.example.backend.domain.fish.dto;

import lombok.*;
import org.example.backend.domain.fish.entity.FishLog;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FishLogResponseDto {

    private Long logId;
    private Long aquariumId;
    private Long fishId;
    private String status;
    private LocalDateTime logDate;

    // Entity -> DTO 변환
    public static FishLogResponseDto from(FishLog fishLog) {
        return FishLogResponseDto.builder()
                .logId(fishLog.getLogId())
                .aquariumId(fishLog.getFish().getAquarium().getId())
                .fishId(fishLog.getFish().getId())
                .status(fishLog.getStatus())
                .logDate(fishLog.getLogDate())
                .build();
    }
}
