package revy.com.client.frankfurter.client.impl;


import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import revy.com.client.frankfurter.client.FrankfurterRestClient;
import revy.com.client.frankfurter.dto.FrankfurterCurrenciesResponse;
import revy.com.client.frankfurter.dto.FrankfurterRateResponse;
import revy.com.client.frankfurter.exception.FrankfurterApiException;

import java.util.List;

/**
 * Spring RestClient로 Frankfurter 외부 환율 API를 호출하는 구현체입니다.
 */
@Slf4j
@Component
public class FrankfurterRestClientImpl implements FrankfurterRestClient {
    private final RestClient restClient;

    public FrankfurterRestClientImpl(RestClient frankfurterRestClient) {
        this.restClient = frankfurterRestClient;
    }

    /**
     * 기준 통화와 상대 통화의 단건 환율을 조회하고 오류 응답은 전용 예외로 변환합니다.
     */
    @Override
    public FrankfurterRateResponse getRate(String base, String quote) {
        FrankfurterRateResponse response = restClient.get()
            .uri("/v2/rate/{base}/{quote}", base, quote)
            .retrieve()
            .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                      (request, responseSpec) -> {
                          String body = new String(responseSpec.getBody().readAllBytes());
                          throw new FrankfurterApiException(responseSpec.getStatusCode().value(), body);
                      })
            .body(FrankfurterRateResponse.class);
        log.debug("[getRate] response : {}", response);
        return response;
    }

    /**
     * 기준 통화와 여러 상대 통화의 최신 환율을 배열 응답으로 조회합니다.
     */
    @Override
    public List<FrankfurterRateResponse> getLatestRates(String base, List<String> quotes) {
        String quoteParam = String.join(",", quotes);
        List<FrankfurterRateResponse> response = restClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/v2/rates")
                .queryParam("base", base)
                .queryParam("quotes", quoteParam)
                .build())
            .retrieve()
            .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                      (request, responseSpec) -> {
                          String body = new String(responseSpec.getBody().readAllBytes());
                          throw new FrankfurterApiException(responseSpec.getStatusCode().value(), body);
                      })
            .body(new ParameterizedTypeReference<>() {});
        log.debug("[getLatestRates] response : {}", response);
        return response;
    }

    /**
     * Frankfurter가 제공하는 지원 통화 메타데이터 목록을 조회합니다.
     */
    @Override
    public List<FrankfurterCurrenciesResponse> getCurrencies() {
        List<FrankfurterCurrenciesResponse> response =  restClient.get()
            .uri("/v2/currencies")
            .retrieve()
            .body(new ParameterizedTypeReference<>() {});

        log.debug("[getCurrencies] response : {}", response);
        return response;
    }

}
