package org.example.backend.domain.point.dto;

import org.example.backend.domain.point.entity.Point;
import org.example.backend.domain.point.entity.TransactionType;

import java.time.LocalDateTime;


public record PointHistoryResponseDto(
    TransactionType type,
    LocalDateTime date,
    Long points,
    Long afterPoint
){
    public static PointHistoryResponseDto from(Point point) {
        return new PointHistoryResponseDto(
                point.getType(),
                point.getCreateDate(),
                point.getPoints(),
                point.getAfterPoint()
        );
    }
}
