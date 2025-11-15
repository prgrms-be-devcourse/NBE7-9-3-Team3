package org.example.backend.domain.trade.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;
import org.example.backend.domain.trade.enums.TradeStatus;

public record TradeRequestDto(
    @NotBlank String title,
    @NotBlank String description,
    @NotNull @PositiveOrZero Long price, // 0원(나눔) 허용
    @NotNull TradeStatus status,
    String category,
    List<String> imageUrls
) {

}