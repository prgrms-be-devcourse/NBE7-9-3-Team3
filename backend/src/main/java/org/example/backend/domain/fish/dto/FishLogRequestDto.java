package org.example.backend.domain.fish.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FishLogRequestDto {

    private Long fishId;
    private String status;
    private LocalDateTime logDate;
}
