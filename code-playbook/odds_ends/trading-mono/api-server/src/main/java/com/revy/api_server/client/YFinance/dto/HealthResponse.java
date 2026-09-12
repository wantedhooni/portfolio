package com.revy.api_server.client.YFinance.dto;

/**
 * 헬스 체크 응답 정보를 담는 DTO.
 *
 * @param status 상태 값
 */
public record HealthResponse(
        String status
) {
}
