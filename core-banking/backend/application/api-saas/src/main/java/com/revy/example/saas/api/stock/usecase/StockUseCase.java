package com.revy.example.saas.api.stock.usecase;

import com.revy.example.core.common.ApiPageResponse;
import com.revy.example.saas.api.stock.payload.StockPayload;
import org.springframework.data.domain.Pageable;

public interface StockUseCase {

    StockPayload.ModelResponse get(Long id);

    ApiPageResponse<StockPayload.ModelResponse> search(Pageable pageable, StockPayload.SearchRequest request);
}
