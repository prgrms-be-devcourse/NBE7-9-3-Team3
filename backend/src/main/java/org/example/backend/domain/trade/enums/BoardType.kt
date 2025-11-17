package org.example.backend.domain.trade.enums

enum class BoardType(
    val description: String
) {
    FISH("물고기"),
    SECONDHAND("중고물품");

    companion object {
        fun from(value: String): BoardType {
            return valueOf(value.uppercase())
        }
    }
}
