package org.example.backend.domain.trade.dto

import org.example.backend.domain.trade.enums.TradeStatus

class TradeSearchRequestDto(
    private val page: Int?,
    private val size: Int?,
    private val sort: String?,
    val searchTerm: String?,
    val searchType: String?,
    val minPrice: Long?,
    val maxPrice: Long?,
    val status: TradeStatus?
) {
    fun getPage(): Int = if (page == null || page < 0) 0 else page

    fun getSize(): Int = if (size == null || size < 1 || size > 100) 10 else size

    fun getSort(): String = if (sort.isNullOrBlank()) "latest" else sort

    fun hasSearchTerm(): Boolean = !searchTerm.isNullOrBlank()

    fun hasPriceFilter(): Boolean = minPrice != null || maxPrice != null

    fun hasStatusFilter(): Boolean = status != null
}
