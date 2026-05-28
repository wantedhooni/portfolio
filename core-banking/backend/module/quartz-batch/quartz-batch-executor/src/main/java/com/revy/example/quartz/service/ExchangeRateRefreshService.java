package com.revy.example.quartz.service;

import java.util.List;

/**
 * 외부 환율 API(Frankfurter)에서 최신 환율을 조회해 DB에 저장하는 서비스.
 */
public interface ExchangeRateRefreshService {

    /**
     * 지정한 상대 통화 목록의 최신 환율을 조회해 저장합니다.
     *
     * @param baseCurrency    기준 통화 코드 (예: USD)
     * @param quoteCurrencies 조회할 상대 통화 목록 (예: [KRW, EUR, JPY, GBP, CNY])
     * @return 처리 결과 요약
     */
    ExchangeRateRefreshResult refresh(String baseCurrency, List<String> quoteCurrencies);

    /**
     * Frankfurter가 제공하는 <b>전체</b> 상대 통화의 최신 환율을 조회해 저장합니다.
     *
     * <p>quotes 파라미터 없이 {@code GET /v2/rates?base={base}} 를 호출하므로
     * 신규 통화쌍이 추가되어도 별도 설정 없이 자동으로 수집됩니다.</p>
     *
     * @param baseCurrency 기준 통화 코드 (예: USD)
     * @return 처리 결과 요약
     */
    ExchangeRateRefreshResult refreshAll(String baseCurrency);
}
