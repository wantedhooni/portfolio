package com.revy.example.admin.api.stock.usecase;

import com.revy.example.admin.api.stock.payload.StockPayload;
import com.revy.example.core.common.ApiPageResponse;
import org.springframework.data.domain.Pageable;

public interface StockUseCase {

    StockPayload.ModelResponse createStock(StockPayload.CreateRequest request);

    StockPayload.ModelResponse get(Long id);

    ApiPageResponse<StockPayload.ModelResponse> search(Pageable pageable, StockPayload.SearchRequest searchRequest);

    StockPayload.ModelResponse updateMarketData(Long id, StockPayload.MarketDataRequest request);

    void delist(Long id);
}
