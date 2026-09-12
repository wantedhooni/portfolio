package com.revy.yfinance;



import com.revy.yfinance.dto.*;

import java.time.LocalDate;
import java.util.Map;

public interface YFinanceProxyClient {

    QuoteResponse getQuote(String symbol);

    Map<String, BulkQuoteItem> getQuotesBulk(String symbolsCsv);

    HistoricalResponse getHistorical(String symbol, LocalDate start, LocalDate end, String interval);

    InfoResponse getInfo(String symbol);

    SnapshotResponse getSnapshot(String symbol);

    EarningsResponse getEarnings(String symbol, String frequency);
}
