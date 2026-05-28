package com.revy.client.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

/**
 * {@code GET /v2/rates} 요청 파라미터를 담는 VO입니다.
 *
 * <ul>
 *   <li>{@code date}는 {@code from}/{@code to}와 함께 사용할 수 없습니다 (API 제약).</li>
 *   <li>{@code group}은 날짜 범위 조회({@code from}/{@code to}) 에서만 유효합니다.</li>
 * </ul>
 *
 * <pre>{@code
 * // 최신 환율 (단순)
 * FrankfurterRatesRequest.ofLatest("USD", List.of("KRW", "EUR"));
 *
 * // 특정 날짜
 * FrankfurterRatesRequest.builder()
 *     .base("USD").quotes(List.of("KRW")).date(LocalDate.of(2026,1,1)).build();
 *
 * // 날짜 범위 + 월별 집계
 * FrankfurterRatesRequest.builder()
 *     .base("USD").quotes(List.of("KRW"))
 *     .from(LocalDate.of(2026,1,1)).to(LocalDate.of(2026,3,31))
 *     .group("month").build();
 * }</pre>
 */
@Getter
@Builder
public class FrankfurterRatesRequest {

    /** 기준 통화 코드 (null → Frankfurter 기본값 EUR) */
    private final String base;

    /** 조회할 상대 통화 목록 (null → 전체 통화) */
    private final List<String> quotes;

    /** 특정 날짜 (YYYY-MM-DD). {@code from}/{@code to}와 함께 사용 불가 */
    private final LocalDate date;

    /** 날짜 범위 시작 (YYYY-MM-DD) */
    private final LocalDate from;

    /** 날짜 범위 종료 (YYYY-MM-DD). 생략 시 오늘 */
    private final LocalDate to;

    /** 다운샘플링 단위: {@code week} | {@code month}. 날짜 범위 조회에서만 유효 */
    private final String group;

    /** 특정 제공자 목록 필터 (null → 전체 제공자 혼합) */
    private final List<String> providers;

    // ── 팩토리 ────────────────────────────────────────────────────

    /** 기준 통화 + 상대 통화 목록의 최신 환율 요청을 생성합니다. */
    public static FrankfurterRatesRequest ofLatest(String base, List<String> quotes) {
        return FrankfurterRatesRequest.builder().base(base).quotes(quotes).build();
    }
}
