package org.example.backend.domain.trade.enums

enum class TradeStatus(
    private val description: String
) {
    SELLING("판매중"),
    COMPLETED("거래완료"),
    CANCELLED("취소됨");
}
