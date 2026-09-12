package com.revy.yfinance.dto;


import java.util.List;

/**
 * 과거 시세 응답 정보를 담는 DTO.
 *
 * @param symbol 종목 심볼
 * @param prices 과거 가격 목록
 */
public record HistoricalResponse(
        String symbol,
        List<HistoricalPrice> prices
) {
}
