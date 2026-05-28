package com.revy.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

/**
 * {@code GET /v2/currency/{code}} 응답 DTO입니다.
 *
 * <p>통화 상세 정보를 반환하며, 고정환율 통화의 경우 {@code peg} 메타데이터가 포함됩니다.</p>
 *
 * @param isoCode    ISO 4217 통화 코드
 * @param isoNumeric ISO 4217 숫자 코드 (null 가능)
 * @param name       통화 전체 이름
 * @param symbol     통화 기호 (null 가능)
 * @param providers  이 통화를 제공하는 공급자 키 목록
 * @param peg        고정환율 메타데이터 (고정환율이 아닌 경우 null)
 */
public record FrankfurterCurrencyDetailResponse(
        @JsonProperty("iso_code")    String    isoCode,
        @JsonProperty("iso_numeric") String    isoNumeric,
                                     String    name,
                                     String    symbol,
                                     List<String> providers,
                                     Peg       peg
) {

    /**
     * 고정환율(페그) 메타데이터입니다.
     *
     * @param base      페그 기준 통화
     * @param rate      페그 환율
     * @param authority 페그를 결정한 기관
     * @param source    출처 URL
     */
    public record Peg(
            String     base,
            BigDecimal rate,
            String     authority,
            String     source
    ) {}
}
