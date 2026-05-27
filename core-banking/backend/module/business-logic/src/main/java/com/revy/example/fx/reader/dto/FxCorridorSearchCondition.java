package com.revy.example.fx.reader.dto;

import com.revy.example.domain.fx.enums.CorridorStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FxCorridorSearchCondition {
    private String         baseCurrencyCode;
    private String         quoteCurrencyCode;
    private CorridorStatus status;
}
