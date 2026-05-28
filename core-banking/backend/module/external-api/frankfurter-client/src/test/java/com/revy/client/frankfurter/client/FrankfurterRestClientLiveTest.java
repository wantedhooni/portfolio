package com.revy.client.frankfurter.client;

import com.revy.client.client.FrankfurterRestClient;
import com.revy.client.client.impl.FrankfurterRestClientImpl;
import com.revy.client.dto.FrankfurterCurrenciesResponse;
import com.revy.client.dto.FrankfurterCurrencyDetailResponse;
import com.revy.client.dto.FrankfurterProviderResponse;
import com.revy.client.dto.FrankfurterRateResponse;
import com.revy.client.dto.FrankfurterRatesRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 실제 Frankfurter v2 서버를 호출해 운영 API 계약과 클라이언트 역직렬화 호환성을 검증하는 통합 테스트입니다.
 *
 * <p>{@code @Tag("live")} 로 표시되므로 일반 {@code ./gradlew test} 에서는 실행되지 않습니다.
 * 실서버 연동이 필요할 때만 {@code ./gradlew test -Plive} 로 명시적으로 실행하세요.</p>
 */
@Tag("live")
@DisplayName("Frankfurter REST 클라이언트 실서버 연동")
class FrankfurterRestClientLiveTest {

    private static final Logger log = LoggerFactory.getLogger(FrankfurterRestClientLiveTest.class);
    private static final String BASE_URL = "https://api.frankfurter.dev";

    private final FrankfurterRestClient client = new FrankfurterRestClientImpl(
        RestClient.builder()
            .baseUrl(BASE_URL)
            .requestFactory(requestFactory())
            .defaultHeader("Accept", "application/json")
            .build()
    );

    @Test
    @DisplayName("실서버에서 최신 단건 환율을 조회한다")
    void getRate_latest() {
        FrankfurterRateResponse r = client.getRate("USD", "KRW");
        log.info("[live] getRate USD/KRW = {}", r);

        assertThat(r.base()).isEqualTo("USD");
        assertThat(r.quote()).isEqualTo("KRW");
        assertThat(r.rate()).isPositive();
        assertThat(r.date()).isNotNull();
    }

    @Test
    @DisplayName("실서버에서 특정 날짜 단건 환율을 조회한다")
    void getRate_withDate() {
        LocalDate targetDate = LocalDate.of(2026, 1, 2); // 최근 거래일
        FrankfurterRateResponse r = client.getRate("USD", "KRW", targetDate);
        log.info("[live] getRate USD/KRW date={} = {}", targetDate, r);

        assertThat(r.base()).isEqualTo("USD");
        assertThat(r.rate()).isPositive();
        assertThat(r.date()).isNotNull();
    }

    @Test
    @DisplayName("실서버에서 전체 최신 환율을 조회한다 (quotes 없이 base만)")
    void getLatestRates_allQuotes() {
        List<FrankfurterRateResponse> rates = client.getLatestRates("USD");
        log.info("[live] getLatestRates USD count={}", rates.size());

        assertThat(rates).isNotEmpty()
                         .allSatisfy(r -> {
                             assertThat(r.base()).isEqualTo("USD");
                             assertThat(r.rate()).isPositive();
                             assertThat(r.date()).isNotNull();
                         });
        // Frankfurter는 EUR·KRW·JPY 등 주요 통화를 포함해야 함
        assertThat(rates).extracting(FrankfurterRateResponse::quote)
                         .contains("EUR", "KRW", "JPY");
    }

    @Test
    @DisplayName("실서버에서 복수 최신 환율을 조회한다")
    void getRates_latest() {
        List<FrankfurterRateResponse> rates = client.getRates("USD", List.of("EUR", "KRW"));
        log.info("[live] getRates USD/EUR,KRW = {}", rates);

        assertThat(rates).hasSize(2)
                         .allSatisfy(r -> {
                             assertThat(r.base()).isEqualTo("USD");
                             assertThat(r.rate()).isPositive();
                         });
        assertThat(rates).extracting(FrankfurterRateResponse::quote)
                         .containsExactlyInAnyOrder("EUR", "KRW");
    }

    @Test
    @DisplayName("실서버에서 날짜 범위 환율 목록을 조회한다")
    void getRates_withDateRange() {
        FrankfurterRatesRequest req = FrankfurterRatesRequest.builder()
            .base("USD").quotes(List.of("KRW"))
            .from(LocalDate.of(2026, 1, 1)).to(LocalDate.of(2026, 1, 31))
            .build();

        List<FrankfurterRateResponse> rates = client.getRates(req);
        log.info("[live] getRates range 2026-01 USD/KRW count={}", rates.size());

        assertThat(rates).isNotEmpty();
        assertThat(rates).allSatisfy(r -> {
            assertThat(r.base()).isEqualTo("USD");
            assertThat(r.quote()).isEqualTo("KRW");
        });
    }

    @Test
    @DisplayName("실서버에서 지원 통화 목록을 조회한다")
    void getCurrencies() {
        List<FrankfurterCurrenciesResponse> currencies = client.getCurrencies();
        log.info("[live] getCurrencies count={}", currencies.size());

        assertThat(currencies).isNotEmpty();
        assertThat(currencies).extracting(FrankfurterCurrenciesResponse::isoCode)
                              .contains("USD", "KRW", "EUR");
        assertThat(currencies).filteredOn(c -> "USD".equals(c.isoCode()))
                              .singleElement()
                              .satisfies(c -> {
                                  assertThat(c.isoNumeric()).isEqualTo("840");
                                  assertThat(c.name()).contains("Dollar");
                              });
    }

    @Test
    @DisplayName("실서버에서 단일 통화 상세를 조회한다")
    void getCurrency() {
        FrankfurterCurrencyDetailResponse detail = client.getCurrency("USD");
        log.info("[live] getCurrency USD = {}", detail);

        assertThat(detail.isoCode()).isEqualTo("USD");
        assertThat(detail.name()).isNotBlank();
        assertThat(detail.providers()).isNotEmpty();
    }

    @Test
    @DisplayName("실서버에서 공급자 목록을 조회한다")
    void getProviders() {
        List<FrankfurterProviderResponse> providers = client.getProviders();
        log.info("[live] getProviders count={}", providers.size());

        assertThat(providers).isNotEmpty();
        assertThat(providers).allSatisfy(p -> {
            assertThat(p.key()).isNotBlank();
            assertThat(p.currencies()).isNotEmpty();
        });
    }

    private static SimpleClientHttpRequestFactory requestFactory() {
        SimpleClientHttpRequestFactory f = new SimpleClientHttpRequestFactory();
        f.setConnectTimeout(Duration.ofSeconds(3));
        f.setReadTimeout(Duration.ofSeconds(5));
        return f;
    }
}
