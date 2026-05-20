package com.revy.example.fx.reader.dto;

import com.revy.example.domain.fx.enums.RateType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@NoArgsConstructor
public class ExchangeRateSearchCondition {

    String baseCurrencyCode;
    String quoteCurrencyCode;
    RateType rateType;
    String source;
    Instant quotedFrom;
    Instant quotedTo;

    @Builder
    public ExchangeRateSearchCondition(String baseCurrencyCode, String quoteCurrencyCode,
                                       RateType rateType, String source,
                                       Instant quotedFrom, Instant quotedTo) {
        this.baseCurrencyCode  = baseCurrencyCode;
        this.quoteCurrencyCode = quoteCurrencyCode;
        this.rateType          = rateType;
        this.source            = source;
        this.quotedFrom        = quotedFrom;
        this.quotedTo          = quotedTo;
    }
}
