package com.revy.api_server.web.api.market;

import com.revy.api_server.application.web.api.market.MarketApi;
import com.revy.api_server.application.web.api.market.usecase.QuoteUsecase;
import com.revy.yfinance.dto.BulkQuoteItem;
import com.revy.yfinance.dto.QuoteResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("MarketApi MockMvc 슬라이스 테스트")
class MarketApiMockMvcTest {

    @Mock
    private QuoteUsecase quoteUsecase;

    @InjectMocks
    private MarketApi marketApi;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(marketApi).build();
    }

    @Test
    @DisplayName("단건 시세 요청 시 심볼을 반환한다")
    void getQuote_returnsSymbol() throws Exception {
        QuoteResponse response = new QuoteResponse("AAPL", BigDecimal.ONE, null, null, null, null, null);
        when(quoteUsecase.getQuote("AAPL")).thenReturn(response);

        mockMvc.perform(get("/api/market/quote/AAPL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol").value("AAPL"));
    }

    @Test
    @DisplayName("벌크 시세 요청 시 심볼 맵을 반환한다")
    void getQuotesBulk_returnsMap() throws Exception {
        Map<String, BulkQuoteItem> response = Map.of(
                "AAPL", new BulkQuoteItem("AAPL", BigDecimal.ONE, null, null, null, null, null, null, null)
        );
        when(quoteUsecase.getQuotesBulk("AAPL")).thenReturn(response);

        mockMvc.perform(get("/api/market/quote").param("symbols", "AAPL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.AAPL.symbol").value("AAPL"));
    }
}
