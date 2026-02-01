package com.revy.yfinance.dto;

/**
 * 레디니스 체크 응답 정보를 담는 DTO.
 *
 * @param status 상태 값
 */
public record ReadyResponse(
        String status
) {
}
