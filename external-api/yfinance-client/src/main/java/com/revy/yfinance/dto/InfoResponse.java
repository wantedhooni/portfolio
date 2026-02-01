package com.revy.yfinance.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * 종목 상세 정보를 담는 DTO.
 *
 * @param symbol             종목 심볼
 * @param shortName          회사 짧은 이름
 * @param longName           회사 전체 이름
 * @param exchange           거래소
 * @param sector             섹터
 * @param industry           산업군
 * @param country            국가
 * @param website            웹사이트
 * @param description        설명
 * @param marketCap          시가총액
 * @param sharesOutstanding  발행 주식 수
 * @param dividendYield      배당 수익률
 * @param fiftyTwoWeekHigh   52주 최고가
 * @param fiftyTwoWeekLow    52주 최저가
 * @param currentPrice       현재가
 * @param trailingPe         PER
 * @param beta               베타
 * @param ceo                CEO
 * @param address            주소
 * @param currency           통화
 */
public record InfoResponse(
        String symbol,
        @JsonProperty("short_name") String shortName,
        @JsonProperty("long_name") String longName,
        String exchange,
        String sector,
        String industry,
        String country,
        String website,
        String description,
        @JsonProperty("market_cap") Long marketCap,
        @JsonProperty("shares_outstanding") Long sharesOutstanding,
        @JsonProperty("dividend_yield") BigDecimal dividendYield,
        @JsonProperty("fifty_two_week_high") BigDecimal fiftyTwoWeekHigh,
        @JsonProperty("fifty_two_week_low")
        BigDecimal fiftyTwoWeekLow,
        @JsonProperty("current_price") BigDecimal currentPrice,
        @JsonProperty("trailing_pe") BigDecimal trailingPe,
        BigDecimal beta,
        String ceo,
        String address,
        String currency
) {
}
