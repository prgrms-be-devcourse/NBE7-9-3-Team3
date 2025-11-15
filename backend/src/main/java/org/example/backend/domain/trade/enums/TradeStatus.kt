package org.example.backend.domain.trade.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TradeStatus {
    SELLING("판매중"),
    COMPLETED("거래완료"),
    CANCELLED("취소됨");

    private final String description;
}
