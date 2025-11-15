package org.example.backend.domain.trade.dto

import org.springframework.data.domain.Page

data class PageResponseDto<T> (
    val content: List<T>,
    val currentPage: Int,
    val pageSize: Int,
    val totalElements: Long,
    val totalPages: Int,
    val first: Boolean,
    val last: Boolean,
    val hasNext: Boolean,
    val hasPrevious: Boolean
){

    companion object {
        @JvmStatic
        fun <T> from(page: Page<T>): PageResponseDto<T> {
            return PageResponseDto(
                content = page.content,
                currentPage = page.number,
                pageSize = page.size,
                totalElements = page.totalElements,
                totalPages = page.totalPages,
                first = page.isFirst,
                last = page.isLast,
                hasNext = page.hasNext(),
                hasPrevious = page.hasPrevious()
            )
        }
    }
}
