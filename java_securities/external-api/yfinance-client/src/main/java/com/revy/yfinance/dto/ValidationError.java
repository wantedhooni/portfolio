package com.revy.yfinance.dto;

import java.util.List;

/**
 * 입력 검증 오류 정보를 담는 DTO.
 *
 * @param loc  오류 위치 정보
 * @param msg  오류 메시지
 * @param type 오류 타입
 */
public record ValidationError(
        List<Object> loc,
        String msg,
        String type
) {
}
