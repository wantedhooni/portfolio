package com.revy.yfinance.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * 종목별 시세 또는 에러 정보를 담는 벌크 응답 항목 DTO.
 *
 * @param symbol        종목 심볼
 * @param currentPrice  현재가
 * @param previousClose 전일 종가
 * @param openPrice     시가
 * @param high          고가
 * @param low           저가
 * @param volume        거래량
 * @param error         에러 메시지
 * @param statusCode    상태 코드
 */
public record BulkQuoteItem(
        String symbol,
        @JsonProperty("current_price") BigDecimal currentPrice,
        @JsonProperty("previous_close")
        BigDecimal previousClose,
        @JsonProperty("open_price") BigDecimal openPrice,
        BigDecimal high,
        BigDecimal low,
        Integer volume,
        String error,
        @JsonProperty("status_code") Integer statusCode
) {
}
