package com.revy.yfinance.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * 스냅샷(정보 + 시세) 응답 정보를 담는 DTO.
 *
 * @param symbol       종목 심볼
 * @param info         종목 상세 정보
 * @param quote        최신 시세
 * @param currentPrice 현재가
 * @param currency     통화
 */
public record SnapshotResponse(
        String symbol,
        InfoResponse info,
        QuoteResponse quote,
        @JsonProperty("current_price")
        BigDecimal currentPrice,
        String currency
) {
}
