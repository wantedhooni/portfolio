package com.revy.api_server.application.web.api.market;

import com.revy.api_server.application.web.api.market.usecase.QuoteUsecase;
import com.revy.yfinance.dto.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/market")
@RequiredArgsConstructor
public class MarketApi {

    private final QuoteUsecase quoteUsecase;

    /**
     * 심볼별 시세를 조회한다.
     */
    @GetMapping("/quote/{symbol}")
    public QuoteResponse getQuote(@PathVariable @Valid @NotEmpty String symbol) {
        return quoteUsecase.getQuote(symbol);
    }

    @GetMapping("/quote")
    public Map<String, BulkQuoteItem> getQuotesBulk(@RequestParam("symbols") @NotBlank String symbolsCsv) {
        return quoteUsecase.getQuotesBulk(symbolsCsv);
    }

    @GetMapping("/historical/{symbol}")
    public HistoricalResponse getHistorical(@PathVariable String symbol, LocalDate start, LocalDate end, String interval) {
        return quoteUsecase.getHistorical(symbol, start, end, interval);
    }

    @GetMapping("/info/{symbol}")
    public InfoResponse getInfo(@PathVariable String symbol) {
        return quoteUsecase.getInfo(symbol);
    }

    @GetMapping("/snapshot/{symbol}")
    public SnapshotResponse getSnapshot(@PathVariable String symbol) {
        return quoteUsecase.getSnapshot(symbol);
    }

    @GetMapping("/earnings/{symbol}")
    public EarningsResponse getEarnings(@PathVariable String symbol, String frequency) {
        return quoteUsecase.getEarnings(symbol, frequency);
    }

}
