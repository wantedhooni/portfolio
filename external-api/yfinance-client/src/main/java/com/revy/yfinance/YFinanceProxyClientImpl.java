package com.revy.yfinance;

import com.revy.yfinance.dto.BulkQuoteItem;
import com.revy.yfinance.dto.EarningsResponse;
import com.revy.yfinance.dto.HistoricalResponse;
import com.revy.yfinance.dto.InfoResponse;
import com.revy.yfinance.dto.QuoteResponse;
import com.revy.yfinance.dto.SnapshotResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class YFinanceProxyClientImpl implements YFinanceProxyClient {
    private final RestClient restClient;

    /**
     * 단일 종목의 최신 시세를 조회한다.
     *
     * @param symbol 종목 심볼
     * @return 최신 시세 응답
     */
    @Override
    public QuoteResponse getQuote(String symbol) {
        return restClient.get()
                         .uri("/quote/{symbol}", symbol)
                         .retrieve()
                         .body(QuoteResponse.class);
    }

    /**
     * 여러 종목의 최신 시세를 CSV로 조회한다.
     *
     * @param symbolsCsv 쉼표로 구분된 종목 심볼 목록
     * @return 심볼별 응답 결과
     */
    @Override
    public Map<String, BulkQuoteItem> getQuotesBulk(String symbolsCsv) {
        return restClient.get()
                         .uri(uriBuilder -> uriBuilder.path("/quote")
                                                      .queryParam("symbols", symbolsCsv)
                                                      .build())
                         .retrieve()
                         .body(new ParameterizedTypeReference<>() {
                         });
    }

    /**
     * 종목의 과거 시세를 조회한다.
     *
     * @param symbol   종목 심볼
     * @param start    시작 날짜
     * @param end      종료 날짜
     * @param interval 조회 간격
     * @return 과거 시세 응답
     */
    @Override
    public HistoricalResponse getHistorical(String symbol, LocalDate start, LocalDate end, String interval) {
        return restClient.get()
                         .uri(uriBuilder -> {
                             var builder = uriBuilder.path("/historical/{symbol}");
                             if (start != null) {
                                 builder.queryParam("start", start);
                             }
                             if (end != null) {
                                 builder.queryParam("end", end);
                             }
                             if (interval != null && !interval.isBlank()) {
                                 builder.queryParam("interval", interval);
                             }
                             return builder.build(symbol);
                         })
                         .retrieve()
                         .body(HistoricalResponse.class);
    }

    /**
     * 종목의 회사 정보를 조회한다.
     *
     * @param symbol 종목 심볼
     * @return 회사 정보 응답
     */
    @Override
    public InfoResponse getInfo(String symbol) {
        return restClient.get()
                         .uri("/info/{symbol}", symbol)
                         .retrieve()
                         .body(InfoResponse.class);
    }

    /**
     * 종목의 스냅샷(정보 + 시세)을 조회한다.
     *
     * @param symbol 종목 심볼
     * @return 스냅샷 응답
     */
    @Override
    public SnapshotResponse getSnapshot(String symbol) {
        return restClient.get()
                         .uri("/snapshot/{symbol}", symbol)
                         .retrieve()
                         .body(SnapshotResponse.class);
    }

    /**
     * 종목의 실적 정보를 조회한다.
     *
     * @param symbol    종목 심볼
     * @param frequency 주기(quarterly 또는 annual)
     * @return 실적 응답
     */
    @Override
    public EarningsResponse getEarnings(String symbol, String frequency) {
        return restClient.get()
                         .uri(uriBuilder -> uriBuilder.path("/earnings/{symbol}")
                                                      .queryParamIfPresent("frequency", Optional.ofNullable(frequency))
                                                      .build(symbol))
                         .retrieve()
                         .body(EarningsResponse.class);
    }
}
