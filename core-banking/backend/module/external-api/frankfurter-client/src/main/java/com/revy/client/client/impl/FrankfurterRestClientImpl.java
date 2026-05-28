package com.revy.client.client.impl;

import com.revy.client.client.FrankfurterRestClient;
import com.revy.client.dto.FrankfurterCurrenciesResponse;
import com.revy.client.dto.FrankfurterCurrencyDetailResponse;
import com.revy.client.dto.FrankfurterProviderResponse;
import com.revy.client.dto.FrankfurterRateResponse;
import com.revy.client.dto.FrankfurterRatesRequest;
import com.revy.client.exception.FrankfurterApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;

/**
 * Spring RestClient로 Frankfurter v2 외부 환율 API를 호출하는 구현체입니다.
 *
 * <p>HTTP 4xx/5xx 응답은 {@link FrankfurterApiException}으로 변환합니다.</p>
 */
@Slf4j
@Component
public class FrankfurterRestClientImpl implements FrankfurterRestClient {

    private final RestClient restClient;

    public FrankfurterRestClientImpl(RestClient frankfurterRestClient) {
        this.restClient = frankfurterRestClient;
    }

    // ── 단건 환율 ────────────────────────────────────────────────

    @Override
    public FrankfurterRateResponse getRate(String base, String quote) {
        return getRate(base, quote, null);
    }

    @Override
    public FrankfurterRateResponse getRate(String base, String quote, LocalDate date) {
        FrankfurterRateResponse response = restClient.get()
            .uri(uriBuilder -> {
                UriBuilder b = uriBuilder.path("/v2/rate/{base}/{quote}");
                if (date != null) b.queryParam("date", date);
                return b.build(base, quote);
            })
            .retrieve()
            .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), errorHandler())
            .body(FrankfurterRateResponse.class);
        log.debug("[getRate] base={} quote={} date={} → {}", base, quote, date, response);
        return response;
    }

    // ── 복수 환율 ────────────────────────────────────────────────

    @Override
    public List<FrankfurterRateResponse> getLatestRates(String base) {
        // quotes 파라미터 없이 호출 → Frankfurter가 제공하는 전체 상대 통화 반환
        List<FrankfurterRateResponse> response = restClient.get()
            .uri(uriBuilder -> uriBuilder.path("/v2/rates").queryParam("base", base).build())
            .retrieve()
            .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), errorHandler())
            .body(new ParameterizedTypeReference<>() {});
        log.debug("[getLatestRates] base={} → {}건", base, response == null ? 0 : response.size());
        return response;
    }

    @Override
    public List<FrankfurterRateResponse> getRates(String base, List<String> quotes) {
        return getRates(FrankfurterRatesRequest.ofLatest(base, quotes));
    }

    @Override
    public List<FrankfurterRateResponse> getRates(FrankfurterRatesRequest request) {
        List<FrankfurterRateResponse> response = restClient.get()
            .uri(uriBuilder -> buildRatesUri(uriBuilder, request))
            .retrieve()
            .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), errorHandler())
            .body(new ParameterizedTypeReference<>() {});
        log.debug("[getRates] request={} → {}건", request, response == null ? 0 : response.size());
        return response;
    }

    // ── 통화 ─────────────────────────────────────────────────────

    @Override
    public List<FrankfurterCurrenciesResponse> getCurrencies() {
        List<FrankfurterCurrenciesResponse> response = restClient.get()
            .uri("/v2/currencies")
            .retrieve()
            .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), errorHandler())
            .body(new ParameterizedTypeReference<>() {});
        log.debug("[getCurrencies] → {}개", response == null ? 0 : response.size());
        return response;
    }

    @Override
    public FrankfurterCurrencyDetailResponse getCurrency(String code) {
        FrankfurterCurrencyDetailResponse response = restClient.get()
            .uri("/v2/currency/{code}", code)
            .retrieve()
            .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), errorHandler())
            .body(FrankfurterCurrencyDetailResponse.class);
        log.debug("[getCurrency] code={} → {}", code, response);
        return response;
    }

    // ── 공급자 ───────────────────────────────────────────────────

    @Override
    public List<FrankfurterProviderResponse> getProviders() {
        List<FrankfurterProviderResponse> response = restClient.get()
            .uri("/v2/providers")
            .retrieve()
            .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), errorHandler())
            .body(new ParameterizedTypeReference<>() {});
        log.debug("[getProviders] → {}개", response == null ? 0 : response.size());
        return response;
    }

    // ── 내부 ─────────────────────────────────────────────────────

    /** /v2/rates 쿼리 파라미터를 request 필드 기준으로 조립합니다. */
    private URI buildRatesUri(UriBuilder b, FrankfurterRatesRequest req) {
        b.path("/v2/rates");
        if (req.getBase()      != null) b.queryParam("base",      req.getBase());
        if (req.getQuotes()    != null && !req.getQuotes().isEmpty()) {
            b.queryParam("quotes", String.join(",", req.getQuotes()));
        }
        if (req.getDate()      != null) b.queryParam("date",      req.getDate());
        if (req.getFrom()      != null) b.queryParam("from",      req.getFrom());
        if (req.getTo()        != null) b.queryParam("to",        req.getTo());
        if (req.getGroup()     != null) b.queryParam("group",     req.getGroup());
        if (req.getProviders() != null && !req.getProviders().isEmpty()) {
            b.queryParam("providers", String.join(",", req.getProviders()));
        }
        return b.build();
    }

    /** HTTP 에러 응답을 {@link FrankfurterApiException}으로 변환하는 핸들러. */
    private RestClient.ResponseSpec.ErrorHandler errorHandler() {
        return (request, responseSpec) -> {
            String body = new String(responseSpec.getBody().readAllBytes());
            throw new FrankfurterApiException(responseSpec.getStatusCode().value(), body);
        };
    }
}
