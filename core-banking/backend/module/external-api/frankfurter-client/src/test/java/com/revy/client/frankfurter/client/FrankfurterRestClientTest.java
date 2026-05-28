package com.revy.client.frankfurter.client;

import com.revy.client.client.FrankfurterRestClient;
import com.revy.client.client.impl.FrankfurterRestClientImpl;
import com.revy.client.dto.FrankfurterCurrenciesResponse;
import com.revy.client.dto.FrankfurterCurrencyDetailResponse;
import com.revy.client.dto.FrankfurterProviderResponse;
import com.revy.client.dto.FrankfurterRateResponse;
import com.revy.client.dto.FrankfurterRatesRequest;
import com.revy.client.exception.FrankfurterApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Frankfurter v2 REST 클라이언트의 URI 생성, 응답 역직렬화, 오류 변환을 검증합니다.
 */
@DisplayName("Frankfurter REST 클라이언트")
class FrankfurterRestClientTest {

    private static final String BASE_URL = "https://api.frankfurter.test";

    private MockRestServiceServer  server;
    private FrankfurterRestClient  client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        server = MockRestServiceServer.bindTo(builder).build();
        client = new FrankfurterRestClientImpl(builder.build());
    }

    // ── 단건 환율 ────────────────────────────────────────────────

    @Test
    @DisplayName("최신 단건 환율을 조회하고 도메인 DTO로 역직렬화한다")
    void getRate_latest() {
        server.expect(requestTo(BASE_URL + "/v2/rate/USD/KRW"))
              .andExpect(method(HttpMethod.GET))
              .andRespond(withSuccess("""
                  {"date":"2026-05-24","base":"USD","quote":"KRW","rate":1372.45}
                  """, MediaType.APPLICATION_JSON));

        FrankfurterRateResponse r = client.getRate("USD", "KRW");

        assertThat(r.rate()).isEqualByComparingTo(new BigDecimal("1372.45"));
        assertThat(r.base()).isEqualTo("USD");
        assertThat(r.quote()).isEqualTo("KRW");
        assertThat(r.date()).isEqualTo(LocalDate.of(2026, 5, 24));
        server.verify();
    }

    @Test
    @DisplayName("특정 날짜 단건 환율 조회 시 date 쿼리 파라미터를 전달한다")
    void getRate_withDate() {
        server.expect(requestTo(BASE_URL + "/v2/rate/USD/KRW?date=2026-01-01"))
              .andExpect(method(HttpMethod.GET))
              .andRespond(withSuccess("""
                  {"date":"2026-01-01","base":"USD","quote":"KRW","rate":1350.00}
                  """, MediaType.APPLICATION_JSON));

        FrankfurterRateResponse r = client.getRate("USD", "KRW", LocalDate.of(2026, 1, 1));

        assertThat(r.date()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(r.rate()).isEqualByComparingTo(new BigDecimal("1350.00"));
        server.verify();
    }

    // ── 복수 환율 ────────────────────────────────────────────────

    @Test
    @DisplayName("전체 최신 환율 조회 시 quotes 없이 base 파라미터만 전달한다")
    void getLatestRates_allQuotes() {
        server.expect(requestTo(BASE_URL + "/v2/rates?base=USD"))
              .andExpect(method(HttpMethod.GET))
              .andRespond(withSuccess("""
                  [
                    {"date":"2026-05-24","base":"USD","quote":"EUR","rate":0.92},
                    {"date":"2026-05-24","base":"USD","quote":"KRW","rate":1372.45},
                    {"date":"2026-05-24","base":"USD","quote":"JPY","rate":156.30}
                  ]
                  """, MediaType.APPLICATION_JSON));

        List<FrankfurterRateResponse> rates = client.getLatestRates("USD");

        assertThat(rates).hasSize(3)
                         .allSatisfy(r -> {
                             assertThat(r.base()).isEqualTo("USD");
                             assertThat(r.rate()).isPositive();
                             assertThat(r.date()).isEqualTo(LocalDate.of(2026, 5, 24));
                         });
        assertThat(rates).extracting(FrankfurterRateResponse::quote)
                         .containsExactlyInAnyOrder("EUR", "KRW", "JPY");
        server.verify();
    }

    @Test
    @DisplayName("복수 환율 조회 시 base·quotes 쿼리 파라미터를 전달하고 배열을 역직렬화한다")
    void getRates_simple() {
        server.expect(requestTo(BASE_URL + "/v2/rates?base=USD&quotes=EUR,KRW"))
              .andExpect(method(HttpMethod.GET))
              .andRespond(withSuccess("""
                  [
                    {"date":"2026-05-24","base":"USD","quote":"EUR","rate":0.92},
                    {"date":"2026-05-24","base":"USD","quote":"KRW","rate":1372.45}
                  ]
                  """, MediaType.APPLICATION_JSON));

        List<FrankfurterRateResponse> rates = client.getRates("USD", List.of("EUR", "KRW"));

        assertThat(rates).hasSize(2)
                         .extracting(FrankfurterRateResponse::quote)
                         .containsExactly("EUR", "KRW");
        assertThat(rates.get(0).rate()).isEqualByComparingTo(new BigDecimal("0.92"));
        server.verify();
    }

    @Test
    @DisplayName("날짜 범위 + 그룹 파라미터가 있는 복수 환율 조회 URI를 올바르게 생성한다")
    void getRates_withDateRange() {
        server.expect(requestTo(BASE_URL + "/v2/rates?base=USD&quotes=KRW&from=2026-01-01&to=2026-03-31&group=month"))
              .andExpect(method(HttpMethod.GET))
              .andRespond(withSuccess("""
                  [
                    {"date":"2026-01-31","base":"USD","quote":"KRW","rate":1340.00},
                    {"date":"2026-02-28","base":"USD","quote":"KRW","rate":1360.00},
                    {"date":"2026-03-31","base":"USD","quote":"KRW","rate":1370.00}
                  ]
                  """, MediaType.APPLICATION_JSON));

        FrankfurterRatesRequest req = FrankfurterRatesRequest.builder()
            .base("USD").quotes(List.of("KRW"))
            .from(LocalDate.of(2026, 1, 1)).to(LocalDate.of(2026, 3, 31))
            .group("month")
            .build();

        List<FrankfurterRateResponse> rates = client.getRates(req);

        assertThat(rates).hasSize(3);
        assertThat(rates).extracting(FrankfurterRateResponse::date)
                         .containsExactly(
                             LocalDate.of(2026, 1, 31),
                             LocalDate.of(2026, 2, 28),
                             LocalDate.of(2026, 3, 31));
        server.verify();
    }

    // ── 통화 ─────────────────────────────────────────────────────

    @Test
    @DisplayName("통화 목록을 조회하고 통화 메타데이터 DTO로 역직렬화한다")
    void getCurrencies() {
        server.expect(requestTo(BASE_URL + "/v2/currencies"))
              .andExpect(method(HttpMethod.GET))
              .andRespond(withSuccess("""
                  [
                    {"iso_code":"USD","iso_numeric":"840","name":"United States Dollar","symbol":"$","start_date":"1999-01-04","end_date":"2026-05-24"},
                    {"iso_code":"KRW","iso_numeric":"410","name":"South Korean Won","symbol":"₩","start_date":"1999-01-04","end_date":"2026-05-24"}
                  ]
                  """, MediaType.APPLICATION_JSON));

        List<FrankfurterCurrenciesResponse> currencies = client.getCurrencies();

        assertThat(currencies).hasSize(2)
                              .extracting(FrankfurterCurrenciesResponse::isoCode)
                              .containsExactly("USD", "KRW");
        assertThat(currencies.get(0).isoNumeric()).isEqualTo("840");
        assertThat(currencies.get(1).symbol()).isEqualTo("₩");
        server.verify();
    }

    @Test
    @DisplayName("단일 통화 상세를 조회하고 공급자 목록·페그 정보를 역직렬화한다")
    void getCurrency() {
        server.expect(requestTo(BASE_URL + "/v2/currency/USD"))
              .andExpect(method(HttpMethod.GET))
              .andRespond(withSuccess("""
                  {
                    "iso_code": "USD",
                    "iso_numeric": "840",
                    "name": "United States Dollar",
                    "symbol": "$",
                    "providers": ["ECB", "BOC", "FED"]
                  }
                  """, MediaType.APPLICATION_JSON));

        FrankfurterCurrencyDetailResponse detail = client.getCurrency("USD");

        assertThat(detail.isoCode()).isEqualTo("USD");
        assertThat(detail.providers()).containsExactly("ECB", "BOC", "FED");
        assertThat(detail.peg()).isNull();
        server.verify();
    }

    // ── 공급자 ───────────────────────────────────────────────────

    @Test
    @DisplayName("공급자 목록을 조회하고 공급자 DTO로 역직렬화한다")
    void getProviders() {
        server.expect(requestTo(BASE_URL + "/v2/providers"))
              .andExpect(method(HttpMethod.GET))
              .andRespond(withSuccess("""
                  [
                    {
                      "key": "ECB",
                      "name": "European Central Bank",
                      "country_code": "EU",
                      "rate_type": "reference",
                      "pivot_currency": "EUR",
                      "start_date": "1999-01-04",
                      "end_date": "2026-05-24",
                      "publishes_missed": 0,
                      "currencies": ["USD","GBP","JPY","KRW"]
                    }
                  ]
                  """, MediaType.APPLICATION_JSON));

        List<FrankfurterProviderResponse> providers = client.getProviders();

        assertThat(providers).hasSize(1);
        FrankfurterProviderResponse ecb = providers.get(0);
        assertThat(ecb.key()).isEqualTo("ECB");
        assertThat(ecb.pivotCurrency()).isEqualTo("EUR");
        assertThat(ecb.publishesMissed()).isZero();
        assertThat(ecb.currencies()).contains("USD", "KRW");
        server.verify();
    }

    // ── 오류 처리 ────────────────────────────────────────────────

    @Test
    @DisplayName("Frankfurter API 오류 응답은 FrankfurterApiException으로 변환한다")
    void getRate_apiError() {
        server.expect(requestTo(BASE_URL + "/v2/rate/USD/UNKNOWN"))
              .andExpect(method(HttpMethod.GET))
              .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                  .contentType(MediaType.APPLICATION_JSON)
                  .body("""
                      {"message":"invalid currency"}
                      """));

        assertThatThrownBy(() -> client.getRate("USD", "UNKNOWN"))
            .isInstanceOfSatisfying(FrankfurterApiException.class, ex -> {
                assertThat(ex.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
                assertThat(ex.responseBody()).contains("invalid currency");
            });
        server.verify();
    }

    @Test
    @DisplayName("404 응답 시 FrankfurterApiException을 던진다")
    void getRates_notFound() {
        server.expect(requestTo(BASE_URL + "/v2/rates?base=USD&quotes=INVALID"))
              .andExpect(method(HttpMethod.GET))
              .andRespond(withStatus(HttpStatus.NOT_FOUND)
                  .contentType(MediaType.APPLICATION_JSON)
                  .body("""
                      {"message":"no data found"}
                      """));

        FrankfurterRatesRequest req = FrankfurterRatesRequest.ofLatest("USD", List.of("INVALID"));

        assertThatThrownBy(() -> client.getRates(req))
            .isInstanceOfSatisfying(FrankfurterApiException.class, ex -> {
                assertThat(ex.statusCode()).isEqualTo(404);
                assertThat(ex.responseBody()).contains("no data found");
            });
        server.verify();
    }
}
