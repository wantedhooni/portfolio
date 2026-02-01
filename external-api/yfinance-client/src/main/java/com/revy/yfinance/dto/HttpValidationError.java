package com.revy.yfinance.dto;



import java.util.List;

/**
 * HTTP 검증 오류 응답 정보를 담는 DTO.
 *
 * @param detail 검증 오류 상세 목록
 */
public record HttpValidationError(
        List<ValidationError> detail
) {
}
