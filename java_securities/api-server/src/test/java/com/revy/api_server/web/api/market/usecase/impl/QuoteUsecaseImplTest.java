package com.revy.api_server.web.api.market.usecase.impl;

import com.revy.api_server.application.web.api.market.usecase.impl.QuoteUsecaseImpl;
import com.revy.yfinance.YFinanceProxyClient;
import com.revy.yfinance.dto.BulkQuoteItem;
import com.revy.yfinance.dto.EarningsResponse;
import com.revy.yfinance.dto.HistoricalResponse;
import com.revy.yfinance.dto.InfoResponse;
import com.revy.yfinance.dto.QuoteResponse;
import com.revy.yfinance.dto.SnapshotResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("QuoteServiceImpl 서비스 테스트")
class QuoteUsecaseImplTest {

    @Mock
    private YFinanceProxyClient yFinanceProxyClient;

    @InjectMocks
    private QuoteUsecaseImpl quoteService;

    @Test
    @DisplayName("단건 시세 조회는 프록시 클라이언트로 위임된다")
    void getQuote_delegatesToClient() {
        QuoteResponse response = new QuoteResponse("AAPL", BigDecimal.ONE, null, null, null, null, null);
        when(yFinanceProxyClient.getQuote("AAPL")).thenReturn(response);

        QuoteResponse result = quoteService.getQuote("AAPL");

        assertThat(result).isSameAs(response);
    }

    @Test
    @DisplayName("벌크 시세 조회는 프록시 클라이언트로 위임된다")
    void getQuotesBulk_delegatesToClient() {
        Map<String, BulkQuoteItem> response = Map.of(
                "AAPL", new BulkQuoteItem("AAPL", BigDecimal.ONE, null, null, null, null, null, null, null)
        );
        when(yFinanceProxyClient.getQuotesBulk("AAPL")).thenReturn(response);

        Map<String, BulkQuoteItem> result = quoteService.getQuotesBulk("AAPL");

        assertThat(result).isEqualTo(response);
    }

    @Test
    @DisplayName("과거 시세 조회는 프록시 클라이언트로 위임된다")
    void getHistorical_delegatesToClient() {
        HistoricalResponse response = new HistoricalResponse("AAPL", List.of());
        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end = LocalDate.of(2024, 2, 1);
        when(yFinanceProxyClient.getHistorical("AAPL", start, end, "1d")).thenReturn(response);

        HistoricalResponse result = quoteService.getHistorical("AAPL", start, end, "1d");

        assertThat(result).isSameAs(response);
    }

    @Test
    @DisplayName("종목 정보 조회는 프록시 클라이언트로 위임된다")
    void getInfo_delegatesToClient() {
        InfoResponse response = new InfoResponse("AAPL", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        when(yFinanceProxyClient.getInfo("AAPL")).thenReturn(response);

        InfoResponse result = quoteService.getInfo("AAPL");

        assertThat(result).isSameAs(response);
    }

    @Test
    @DisplayName("스냅샷 조회는 프록시 클라이언트로 위임된다")
    void getSnapshot_delegatesToClient() {
        SnapshotResponse response = new SnapshotResponse("AAPL", null, null, null, "USD");
        when(yFinanceProxyClient.getSnapshot("AAPL")).thenReturn(response);

        SnapshotResponse result = quoteService.getSnapshot("AAPL");

        assertThat(result).isSameAs(response);
    }

    @Test
    @DisplayName("실적 조회는 프록시 클라이언트로 위임된다")
    void getEarnings_delegatesToClient() {
        EarningsResponse response = new EarningsResponse("AAPL", "quarterly", List.of(), null, null);
        when(yFinanceProxyClient.getEarnings("AAPL", "quarterly")).thenReturn(response);

        EarningsResponse result = quoteService.getEarnings("AAPL", "quarterly");

        assertThat(result).isSameAs(response);
    }
}
