package com.revy.client.client;


import com.revy.client.dto.FrankfurterCurrenciesResponse;
import com.revy.client.dto.FrankfurterRateResponse;

import java.util.List;

/**
 * Frankfurter 외부 환율 API를 호출하는 REST 클라이언트 계약입니다.
 */
public interface FrankfurterRestClient{

    /**
     * 기준 통화와 상대 통화의 단건 환율을 조회합니다.
     */
    FrankfurterRateResponse getRate(String base, String quote);

    /**
     * 기준 통화와 여러 상대 통화의 최신 환율 목록을 조회합니다.
     */
    List<FrankfurterRateResponse> getLatestRates(String base, List<String> quotes);

    /**
     * Frankfurter에서 제공하는 지원 통화 목록을 조회합니다.
     */
    List<FrankfurterCurrenciesResponse> getCurrencies();
}
