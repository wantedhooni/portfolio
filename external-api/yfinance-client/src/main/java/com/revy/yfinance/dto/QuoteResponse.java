package com.revy.yfinance.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * 최신 시세 응답 정보를 담는 DTO.
 *
 * @param symbol        종목 심볼
 * @param currentPrice  현재가
 * @param previousClose 전일 종가
 * @param openPrice     시가
 * @param high          고가
 * @param low           저가
 * @param volume        거래량
 */
public record QuoteResponse(
        String symbol,
        @JsonProperty("current_price")
        BigDecimal currentPrice,
        @JsonProperty("previous_close") BigDecimal previousClose,
        @JsonProperty("open_price") BigDecimal openPrice,
        BigDecimal high,
        BigDecimal low,
        Integer volume
) {
}
