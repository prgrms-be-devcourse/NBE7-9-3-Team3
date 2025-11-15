package org.example.backend.domain.trade.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.PositiveOrZero
import org.example.backend.domain.trade.enums.TradeStatus

data class TradeRequestDto(
    @field:NotBlank @JvmField val title: String,
    @field:NotBlank @JvmField val description: String,
    @field:NotNull @field:PositiveOrZero @JvmField val price: Long,  // 0원(나눔) 허용
    @field:NotNull @JvmField val status: TradeStatus,
    @JvmField val category: String?,
    @JvmField val imageUrls: List<String>
) 