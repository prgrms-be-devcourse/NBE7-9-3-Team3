package org.example.backend.domain.point.dto;

public record PurchaseRequestDto(
        Long sellerId,
        Long amount,
        Long tradeId
) {}
