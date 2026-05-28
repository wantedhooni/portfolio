package com.revy.client.client;

import com.revy.client.dto.FrankfurterCurrenciesResponse;
import com.revy.client.dto.FrankfurterCurrencyDetailResponse;
import com.revy.client.dto.FrankfurterProviderResponse;
import com.revy.client.dto.FrankfurterRateResponse;
import com.revy.client.dto.FrankfurterRatesRequest;

import java.time.LocalDate;
import java.util.List;

/**
 * Frankfurter v2 외부 환율 API를 호출하는 REST 클라이언트 계약입니다.
 *
 * <p>모든 메서드는 HTTP 4xx/5xx 오류 시 {@link com.revy.client.exception.FrankfurterApiException}을 던집니다.</p>
 */
public interface FrankfurterRestClient {

    // ── 단건 환율 (/v2/rate/{base}/{quote}) ──────────────────────

    /**
     * 기준 통화와 상대 통화의 최신 환율을 조회합니다.
     */
    FrankfurterRateResponse getRate(String base, String quote);

    /**
     * 기준 통화와 상대 통화의 특정 날짜 환율을 조회합니다.
     *
     * @param date 조회 날짜 (YYYY-MM-DD). null이면 최신 환율 반환
     */
    FrankfurterRateResponse getRate(String base, String quote, LocalDate date);

    // ── 복수 환율 (/v2/rates) ────────────────────────────────────

    /**
     * 기준 통화 기준으로 Frankfurter가 제공하는 <b>모든</b> 상대 통화의 최신 환율을 조회합니다.
     *
     * <p>{@code GET /v2/rates?base={base}} — quotes 파라미터 없이 전체 통화를 반환합니다.</p>
     *
     * @param base 기준 통화 코드 (예: USD)
     */
    List<FrankfurterRateResponse> getLatestRates(String base);

    /**
     * 기준 통화와 지정한 상대 통화들의 최신 환율 목록을 조회합니다.
     *
     * @param base   기준 통화 코드 (예: USD)
     * @param quotes 상대 통화 목록 (예: [KRW, EUR, JPY])
     */
    List<FrankfurterRateResponse> getRates(String base, List<String> quotes);

    /**
     * 풍부한 파라미터({@code from}/{@code to}/{@code date}/{@code group}/{@code providers})로
     * 환율 목록을 조회합니다.
     *
     * @see FrankfurterRatesRequest
     */
    List<FrankfurterRateResponse> getRates(FrankfurterRatesRequest request);

    // ── 통화 ─────────────────────────────────────────────────────

    /**
     * Frankfurter에서 제공하는 지원 통화 목록을 조회합니다.
     */
    List<FrankfurterCurrenciesResponse> getCurrencies();

    /**
     * 단일 통화의 상세 정보를 조회합니다 (공급자 목록, 고정환율 메타데이터 포함).
     *
     * @param code ISO 4217 통화 코드 (예: USD)
     */
    FrankfurterCurrencyDetailResponse getCurrency(String code);

    // ── 공급자 ───────────────────────────────────────────────────

    /**
     * 이용 가능한 환율 데이터 공급자 목록을 조회합니다.
     */
    List<FrankfurterProviderResponse> getProviders();
}
