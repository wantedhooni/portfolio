package revy.com.client.frankfurter.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

/**
 * Frankfurter 지원 통화 목록 API의 개별 통화 응답 정보를 표현하는 DTO입니다.
 */
public record FrankfurterCurrenciesResponse(
    @JsonProperty("iso_code")
    String isoCode,

    @JsonProperty("iso_numeric")
    String isoNumeric,

    String name,

    String symbol,

    @JsonProperty("start_date")
    LocalDate startDate,

    @JsonProperty("end_date")
    LocalDate endDate
) {
}
