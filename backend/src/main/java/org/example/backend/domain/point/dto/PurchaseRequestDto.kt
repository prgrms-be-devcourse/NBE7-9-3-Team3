package org.example.backend.domain.point.dto


data class PurchaseRequestDto(
    val sellerId: Long?,
    val amount: Long?,
    val tradeId: Long?
)
