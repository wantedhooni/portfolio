package com.revy.api_server.application.web.api.market.usecase;


import com.revy.yfinance.dto.BulkQuoteItem;
import com.revy.yfinance.dto.EarningsResponse;
import com.revy.yfinance.dto.HistoricalResponse;
import com.revy.yfinance.dto.InfoResponse;
import com.revy.yfinance.dto.QuoteResponse;
import com.revy.yfinance.dto.SnapshotResponse;

import java.time.LocalDate;
import java.util.Map;

public interface QuoteUsecase {

    QuoteResponse getQuote(String symbol);

    Map<String, BulkQuoteItem> getQuotesBulk(String symbolsCsv);

    HistoricalResponse getHistorical(String symbol, LocalDate start, LocalDate end, String interval);

    InfoResponse getInfo(String symbol);

    SnapshotResponse getSnapshot(String symbol);

    EarningsResponse getEarnings(String symbol, String frequency);
}
