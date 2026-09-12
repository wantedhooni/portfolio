package com.revy.api_server.client.YFinance.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.LocalDate;

/**
 * 과거 가격 정보를 담는 DTO.
 *
 * @param date      가격 날짜
 * @param timestamp 가격 타임스탬프
 * @param open      시가
 * @param high      고가
 * @param low       저가
 * @param close     종가
 * @param volume    거래량
 */
public record HistoricalPrice(
        LocalDate date,
        OffsetDateTime timestamp,
        BigDecimal open,
        BigDecimal high,
        BigDecimal low,
        BigDecimal close,
        Integer volume
) {
}
