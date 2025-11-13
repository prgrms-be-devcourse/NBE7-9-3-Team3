package org.example.backend.domain.trade.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BoardType {
    FISH("물고기"),
    SECONDHAND("중고물품");

    private final String description;

    public static BoardType from(String value) {
        return BoardType.valueOf(value.toUpperCase());
    }
}
