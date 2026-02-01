package com.revy.api_server.application.web.api.market.usecase.impl;

import com.revy.api_server.application.web.api.market.usecase.QuoteUsecase;
import com.revy.yfinance.YFinanceProxyClient;
import com.revy.yfinance.dto.BulkQuoteItem;
import com.revy.yfinance.dto.EarningsResponse;
import com.revy.yfinance.dto.HistoricalResponse;
import com.revy.yfinance.dto.InfoResponse;
import com.revy.yfinance.dto.QuoteResponse;
import com.revy.yfinance.dto.SnapshotResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class QuoteUsecaseImpl implements QuoteUsecase {

    private final YFinanceProxyClient yFinanceProxyClient;


    @Override
    public QuoteResponse getQuote(String symbol) {
        return yFinanceProxyClient.getQuote(symbol);
    }

    @Override
    public Map<String, BulkQuoteItem> getQuotesBulk(String symbolsCsv) {
        return yFinanceProxyClient.getQuotesBulk(symbolsCsv);
    }

    @Override
    public HistoricalResponse getHistorical(String symbol, LocalDate start, LocalDate end, String interval) {
        return yFinanceProxyClient.getHistorical(symbol, start, end, interval);
    }

    @Override
    public InfoResponse getInfo(String symbol) {
        return yFinanceProxyClient.getInfo(symbol);
    }

    @Override
    public SnapshotResponse getSnapshot(String symbol) {
        return yFinanceProxyClient.getSnapshot(symbol);
    }

    @Override
    public EarningsResponse getEarnings(String symbol, String frequency) {
        return yFinanceProxyClient.getEarnings(symbol, frequency);
    }
}
