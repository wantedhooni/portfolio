package com.revy.yfinance.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 실적 응답 정보를 담는 DTO.
 *
 * @param symbol           종목 심볼
 * @param frequency        조회 주기
 * @param rows             실적 목록
 * @param nextEarningsDate 다음 실적 예정일
 * @param lastEps          마지막 EPS
 */
public record EarningsResponse(
        String symbol,
        String frequency,
        List<EarningRow> rows,
        @JsonProperty("next_earnings_date") LocalDate nextEarningsDate,
        @JsonProperty("last_eps")
        BigDecimal lastEps
) {
}
