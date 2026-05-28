package com.revy.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.List;

/**
 * {@code GET /v2/providers} 응답의 개별 공급자 DTO입니다.
 *
 * @param key              공급자 식별 키 (예: ECB, BOC)
 * @param name             공급자 전체 이름
 * @param countryCode      ISO 3166-1 alpha-2 국가 코드 (null 가능)
 * @param rateType         공급자가 게시하는 환율 유형 (null 가능)
 * @param pivotCurrency    공급자의 기준(피벗) 통화 (null 가능)
 * @param dataUrl          데이터 소스 URL (null 가능)
 * @param termsUrl         이용 약관 URL (null 가능)
 * @param startDate        가장 이른 이용 가능 날짜 (null 가능)
 * @param endDate          가장 최근 이용 가능 날짜 (null 가능)
 * @param publishesMissed  마감일 이후 누락된 게시 횟수 (null → 주기 없음)
 * @param currencies       이 공급자가 지원하는 통화 코드 목록
 */
public record FrankfurterProviderResponse(
        String     key,
        String     name,
        @JsonProperty("country_code")      String     countryCode,
        @JsonProperty("rate_type")         String     rateType,
        @JsonProperty("pivot_currency")    String     pivotCurrency,
        @JsonProperty("data_url")          String     dataUrl,
        @JsonProperty("terms_url")         String     termsUrl,
        @JsonProperty("start_date")        LocalDate  startDate,
        @JsonProperty("end_date")          LocalDate  endDate,
        @JsonProperty("publishes_missed")  Integer    publishesMissed,
        List<String> currencies
) {}
