package org.example.backend.domain.trade.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.PositiveOrZero
import org.example.backend.domain.trade.enums.TradeStatus

data class TradeRequestDto(
    @field:NotBlank val title: String,
    @field:NotBlank val description: String,
    @field:NotNull @field:PositiveOrZero val price: Long,  // 0원(나눔) 허용
    @field:NotNull val status: TradeStatus,
    val category: String?,
    val imageUrls: List<String>
) 