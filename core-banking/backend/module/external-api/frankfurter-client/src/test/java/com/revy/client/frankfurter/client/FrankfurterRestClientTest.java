package com.revy.client.frankfurter.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import com.revy.client.client.FrankfurterRestClient;
import com.revy.client.client.impl.FrankfurterRestClientImpl;
import com.revy.client.dto.FrankfurterCurrenciesResponse;
import com.revy.client.dto.FrankfurterRateResponse;
import com.revy.client.exception.FrankfurterApiException;

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
 * Frankfurter 외부 환율 API REST 클라이언트의 URI 생성, 응답 역직렬화, 오류 변환을 검증하는 테스트 클래스입니다.
 */
@DisplayName("Frankfurter REST 클라이언트")
class FrankfurterRestClientTest {

    private MockRestServiceServer server;
    private FrankfurterRestClient frankfurterRestClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder()
            .baseUrl("https://api.frankfurter.test");
        server = MockRestServiceServer.bindTo(builder).build();
        frankfurterRestClient = new FrankfurterRestClientImpl(builder.build());
    }

    @Test
    @DisplayName("단건 환율을 조회하고 응답을 도메인 DTO로 변환한다")
    void getRate() {
        server.expect(requestTo("https://api.frankfurter.test/v2/rate/USD/KRW"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess("""
                {
                  "rate": 1372.45,
                  "base": "USD",
                  "quote": "KRW",
                  "date": "2026-05-24"
                }
                """, MediaType.APPLICATION_JSON));

        FrankfurterRateResponse response = frankfurterRestClient.getRate("USD", "KRW");

        assertThat(response.rate()).isEqualByComparingTo(new BigDecimal("1372.45"));
        assertThat(response.base()).isEqualTo("USD");
        assertThat(response.quote()).isEqualTo("KRW");
        assertThat(response.date()).isEqualTo(LocalDate.of(2026, 5, 24));
        server.verify();
    }

    @Test
    @DisplayName("최신 환율 목록 조회 시 기준 통화와 대상 통화를 쿼리 파라미터로 전달한다")
    void getLatestRates() {
        server.expect(requestTo("https://api.frankfurter.test/v2/rates?base=USD&quotes=EUR,KRW"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess("""
                [
                  {
                    "rate": 0.92,
                    "base": "USD",
                    "quote": "EUR",
                    "date": "2026-05-24"
                  },
                  {
                    "rate": 1372.45,
                    "base": "USD",
                    "quote": "KRW",
                    "date": "2026-05-24"
                  }
                ]
                """, MediaType.APPLICATION_JSON));

        List<FrankfurterRateResponse> response = frankfurterRestClient.getLatestRates("USD", List.of("EUR", "KRW"));

        assertThat(response).hasSize(2)
            .extracting(FrankfurterRateResponse::quote)
            .containsExactly("EUR", "KRW");
        assertThat(response.get(0).base()).isEqualTo("USD");
        assertThat(response.get(0).rate()).isEqualByComparingTo(new BigDecimal("0.92"));
        assertThat(response.get(0).date()).isEqualTo(LocalDate.of(2026, 5, 24));
        assertThat(response.get(1).rate()).isEqualByComparingTo(new BigDecimal("1372.45"));
        server.verify();
    }

    @Test
    @DisplayName("지원 통화 목록을 통화 메타데이터 DTO로 변환한다")
    void getCurrencies() {
        server.expect(requestTo("https://api.frankfurter.test/v2/currencies"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess("""
                [
                  {
                    "iso_code": "USD",
                    "iso_numeric": "840",
                    "name": "United States Dollar",
                    "symbol": "$",
                    "start_date": "1999-01-04",
                    "end_date": "2026-05-24"
                  },
                  {
                    "iso_code": "KRW",
                    "iso_numeric": "410",
                    "name": "South Korean Won",
                    "symbol": "₩",
                    "start_date": "1999-01-04",
                    "end_date": "2026-05-24"
                  }
                ]
                """, MediaType.APPLICATION_JSON));

        List<FrankfurterCurrenciesResponse> response = frankfurterRestClient.getCurrencies();

        assertThat(response).hasSize(2)
            .extracting(FrankfurterCurrenciesResponse::isoCode)
            .containsExactly("USD", "KRW");
        assertThat(response.get(0).name()).isEqualTo("United States Dollar");
        assertThat(response.get(1).isoNumeric()).isEqualTo("410");
        server.verify();
    }

    @Test
    @DisplayName("Frankfurter API 오류 응답은 전용 예외로 변환한다")
    void getRateWhenApiReturnsError() {
        server.expect(requestTo("https://api.frankfurter.test/v2/rate/USD/UNKNOWN"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                    {
                      "message": "invalid currency"
                    }
                    """));

        assertThatThrownBy(() -> frankfurterRestClient.getRate("USD", "UNKNOWN"))
            .isInstanceOfSatisfying(FrankfurterApiException.class, exception -> {
                assertThat(exception.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
                assertThat(exception.responseBody()).contains("invalid currency");
            });
        server.verify();
    }
}
