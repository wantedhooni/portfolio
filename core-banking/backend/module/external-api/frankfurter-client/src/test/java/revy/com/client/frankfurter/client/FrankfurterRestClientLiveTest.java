package revy.com.client.frankfurter.client;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import revy.com.client.frankfurter.client.impl.FrankfurterRestClientImpl;
import revy.com.client.frankfurter.dto.FrankfurterCurrenciesResponse;
import revy.com.client.frankfurter.dto.FrankfurterRateResponse;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 실제 Frankfurter 서버를 호출해 운영 API 계약과 클라이언트 역직렬화 호환성을 검증하는 통합 테스트 클래스입니다.
 *
 * <p>{@code @Tag("live")} 태그로 표시되어 있으므로 일반 {@code ./gradlew test} 에서는 실행되지 않습니다.
 * 실서버 연동이 필요할 때만 {@code ./gradlew test -Plive} 로 명시적으로 실행하세요.</p>
 */
@Tag("live")
@DisplayName("Frankfurter REST 클라이언트 실서버 연동")
class FrankfurterRestClientLiveTest {

    private static final Logger log = LoggerFactory.getLogger(FrankfurterRestClientLiveTest.class);
    private static final String FRANKFURTER_BASE_URL = "https://api.frankfurter.dev";

    private final FrankfurterRestClient frankfurterRestClient = new FrankfurterRestClientImpl(
        RestClient.builder()
            .baseUrl(FRANKFURTER_BASE_URL)
            .requestFactory(requestFactory())
            .defaultHeader("Accept", "application/json")
            .build()
    );

    /**
     * 실서버에서 단건 USD/KRW 환율을 조회해 응답 필수 필드가 채워지는지 확인합니다.
     */
    @Test
    @DisplayName("실서버에서 단건 환율을 조회한다")
    void getRateFromLiveServer() {
        FrankfurterRateResponse response = frankfurterRestClient.getRate("USD", "KRW");

        assertThat(response.base()).isEqualTo("USD");
        assertThat(response.quote()).isEqualTo("KRW");
        assertThat(response.rate()).isPositive();
        assertThat(response.date()).isNotNull();
    }

    /**
     * 실서버에서 여러 최신 환율을 조회해 요청한 상대 통화가 모두 포함되는지 확인합니다.
     */
    @Test
    @DisplayName("실서버에서 최신 환율 목록을 조회한다")
    void getLatestRatesFromLiveServer() {
        List<FrankfurterRateResponse> response = frankfurterRestClient.getLatestRates("USD", List.of("EUR", "KRW"));

        assertThat(response).hasSize(2)
            .allSatisfy(rate -> {
                assertThat(rate.base()).isEqualTo("USD");
                assertThat(rate.rate()).isPositive();
                assertThat(rate.date()).isNotNull();
            });
        assertThat(response).extracting(FrankfurterRateResponse::quote)
            .containsExactlyInAnyOrder("EUR", "KRW");
    }

    /**
     * 실서버에서 지원 통화 목록을 조회해 핵심 통화 메타데이터가 포함되는지 확인합니다.
     */
    @Test
    @DisplayName("실서버에서 지원 통화 목록을 조회한다")
    void getCurrenciesFromLiveServer() {
        List<FrankfurterCurrenciesResponse> response = frankfurterRestClient.getCurrencies();
        log.info("[live] getCurrencies response: {}", response);
        assertThat(response).isNotEmpty();
        assertThat(response).extracting(FrankfurterCurrenciesResponse::isoCode)
            .contains("USD", "KRW", "EUR");
        assertThat(response).filteredOn(currency -> "USD".equals(currency.isoCode()))
            .singleElement()
            .satisfies(currency -> {
                assertThat(currency.isoNumeric()).isEqualTo("840");
                assertThat(currency.name()).contains("Dollar");
                assertThat(currency.startDate()).isNotNull();
                assertThat(currency.endDate()).isNotNull();
            });
    }

    private static SimpleClientHttpRequestFactory requestFactory() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(3));
        requestFactory.setReadTimeout(Duration.ofSeconds(5));
        return requestFactory;
    }
}
