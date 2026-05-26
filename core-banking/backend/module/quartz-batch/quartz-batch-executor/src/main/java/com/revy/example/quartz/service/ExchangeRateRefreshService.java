package com.revy.example.quartz.service;

import java.util.List;

/**
 * 외부 환율 API(Frankfurter)에서 최신 환율을 조회해 DB에 저장하는 서비스.
 */
public interface ExchangeRateRefreshService {

    /**
     * @param baseCurrency    기준 통화 코드 (예: USD)
     * @param quoteCurrencies 조회할 상대 통화 목록 (예: [KRW, EUR, JPY, GBP, CNY])
     * @return 처리 결과 요약
     */
    ExchangeRateRefreshResult refresh(String baseCurrency, List<String> quoteCurrencies);
}
