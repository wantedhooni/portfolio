package com.revy.yfinance.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 개별 실적 정보를 담는 DTO.
 *
 * @param earningsDate   실적 발표일
 * @param reportedEps    실제 EPS
 * @param estimatedEps   예상 EPS
 * @param revenue        매출
 * @param surprise       서프라이즈
 * @param surprisePercent 서프라이즈 비율
 */
public record EarningRow(
        @JsonProperty("earnings_date") LocalDate earningsDate,
        @JsonProperty("reported_eps")
        BigDecimal reportedEps,
        @JsonProperty("estimated_eps") BigDecimal estimatedEps,
        BigDecimal revenue,
        BigDecimal surprise,
        @JsonProperty("surprise_percent") BigDecimal surprisePercent
) {
}
