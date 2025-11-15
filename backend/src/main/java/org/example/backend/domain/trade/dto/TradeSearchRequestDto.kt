package org.example.backend.domain.trade.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.example.backend.domain.trade.enums.TradeStatus;

public record TradeSearchRequestDto(
    // 페이징
    @Min(0) Integer page,
    @Min(1) @Max(100) Integer size,
    String sort,

    // 검색 (추후 구현)
    String searchTerm,
    String searchType,

    // 필터 (추후 구현)
    Integer minPrice,
    Integer maxPrice,
    TradeStatus status
) {

    // 기본값 설정
    public TradeSearchRequestDto {
        page = (page == null || page < 0) ? 0 : page;
        size = (size == null || size < 1 || size > 100) ? 10 : size;
        sort = (sort == null || sort.isBlank()) ? "latest" : sort;
    }

    // 현재는 페이징만 사용, 나중에 검색/필터 기능 확장 가능
    public boolean hasSearchTerm() {
        return searchTerm != null && !searchTerm.isBlank();
    }

    public boolean hasPriceFilter() {
        return minPrice != null || maxPrice != null;
    }

    public boolean hasStatusFilter() {
        return status != null;
    }
}
