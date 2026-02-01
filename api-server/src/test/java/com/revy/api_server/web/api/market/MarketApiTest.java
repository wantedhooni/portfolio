package com.revy.api_server.web.api.market;

import com.revy.api_server.application.web.api.market.MarketApi;
import com.revy.api_server.application.web.api.market.usecase.QuoteUsecase;
import com.revy.yfinance.dto.BulkQuoteItem;
import com.revy.yfinance.dto.EarningRow;
import com.revy.yfinance.dto.EarningsResponse;
import com.revy.yfinance.dto.HistoricalPrice;
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
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("MarketApi 컨트롤러 단위 테스트")
class MarketApiTest {

    @Mock
    private QuoteUsecase quoteUsecase;

    @InjectMocks
    private MarketApi marketApi;

    @Test
    @DisplayName("단건 시세 응답을 반환한다")
    void getQuote_returnsQuote() throws Exception {
        QuoteResponse response = new QuoteResponse("AAPL", BigDecimal.ONE, null, null, null, null, null);
        when(quoteUsecase.getQuote("AAPL")).thenReturn(response);

        QuoteResponse result = marketApi.getQuote("AAPL");

        assertThat(result.symbol()).isEqualTo("AAPL");
    }

    @Test
    @DisplayName("벌크 시세 응답을 반환한다")
    void getQuotesBulk_returnsMap() throws Exception {
        Map<String, BulkQuoteItem> response = Map.of(
                "AAPL", new BulkQuoteItem("AAPL", BigDecimal.ONE, null, null, null, null, null, null, null)
        );
        when(quoteUsecase.getQuotesBulk("AAPL")).thenReturn(response);

        Map<String, BulkQuoteItem> result = marketApi.getQuotesBulk("AAPL");

        assertThat(result.get("AAPL").symbol()).isEqualTo("AAPL");
    }

    @Test
    @DisplayName("과거 시세 응답을 반환한다")
    void getHistorical_returnsHistory() throws Exception {
        HistoricalPrice price = new HistoricalPrice(
                LocalDate.of(2024, 1, 1),
                OffsetDateTime.parse("2024-01-01T00:00:00Z"),
                BigDecimal.ONE,
                BigDecimal.TEN,
                BigDecimal.ONE,
                BigDecimal.TEN,
                100
        );
        HistoricalResponse response = new HistoricalResponse("AAPL", List.of(price));
        when(quoteUsecase.getHistorical("AAPL", null, null, null)).thenReturn(response);

        HistoricalResponse result = marketApi.getHistorical("AAPL", null, null, null);

        assertThat(result.symbol()).isEqualTo("AAPL");
        assertThat(result.prices()).hasSize(1);
    }

    @Test
    @DisplayName("종목 정보 응답을 반환한다")
    void getInfo_returnsInfo() throws Exception {
        InfoResponse response = new InfoResponse("AAPL", "short", "long", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, "USD");
        when(quoteUsecase.getInfo("AAPL")).thenReturn(response);

        InfoResponse result = marketApi.getInfo("AAPL");

        assertThat(result.symbol()).isEqualTo("AAPL");
    }

    @Test
    @DisplayName("스냅샷 응답을 반환한다")
    void getSnapshot_returnsSnapshot() throws Exception {
        SnapshotResponse response = new SnapshotResponse("AAPL", null, null, BigDecimal.ONE, "USD");
        when(quoteUsecase.getSnapshot("AAPL")).thenReturn(response);

        SnapshotResponse result = marketApi.getSnapshot("AAPL");

        assertThat(result.symbol()).isEqualTo("AAPL");
    }

    @Test
    @DisplayName("실적 응답을 반환한다")
    void getEarnings_returnsEarnings() throws Exception {
        EarningRow row = new EarningRow(LocalDate.of(2024, 1, 1), BigDecimal.ONE, null, null, null, null);
        EarningsResponse response = new EarningsResponse("AAPL", "quarterly", List.of(row), null, null);
        when(quoteUsecase.getEarnings("AAPL", null)).thenReturn(response);

        EarningsResponse result = marketApi.getEarnings("AAPL", null);

        assertThat(result.symbol()).isEqualTo("AAPL");
        assertThat(result.frequency()).isEqualTo("quarterly");
    }
}
