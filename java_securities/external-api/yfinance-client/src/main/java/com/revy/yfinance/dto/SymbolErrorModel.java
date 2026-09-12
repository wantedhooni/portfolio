package com.revy.yfinance.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 종목별 에러 정보를 담는 DTO.
 *
 * @param error      에러 메시지
 * @param statusCode 상태 코드
 */
public record SymbolErrorModel(
        String error,
        @JsonProperty("status_code") int statusCode
) {
}
