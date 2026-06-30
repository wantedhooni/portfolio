package com.revy.api_server.application.web.api.market.usecase;


import com.revy.api_server.client.YFinance.dto.BulkQuoteItem;
import com.revy.api_server.client.YFinance.dto.EarningsResponse;
import com.revy.api_server.client.YFinance.dto.HistoricalResponse;
import com.revy.api_server.client.YFinance.dto.InfoResponse;
import com.revy.api_server.client.YFinance.dto.QuoteResponse;
import com.revy.api_server.client.YFinance.dto.SnapshotResponse;

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
