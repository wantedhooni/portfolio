package com.revy.example.saas.api.stock.usecase.impl;

import com.revy.example.core.common.ApiPageResponse;
import com.revy.example.domain.account.exception.StockNotFoundException;
import com.revy.example.saas.api.stock.payload.StockPayload;
import com.revy.example.saas.api.stock.usecase.StockUseCase;
import com.revy.example.stock.reader.StockReader;
import com.revy.example.stock.reader.dto.StockResult;
import com.revy.example.stock.reader.dto.StockSearchCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class StockUseCaseImpl implements StockUseCase {

    private final StockReader stockReader;

    @Override
    @Transactional(readOnly = true)
    public StockPayload.ModelResponse get(Long id) {
        return stockReader.findById(id)
            .filter(StockResult::isActive)
            .map(this::toModelResponse)
            .orElseThrow(StockNotFoundException::new);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiPageResponse<StockPayload.ModelResponse> search(Pageable pageable, StockPayload.SearchRequest request) {
        StockSearchCondition condition = StockSearchCondition.builder()
            .ticker(request.ticker())
            .name(request.name())
            .exchange(request.exchange())
            .sector(request.sector())
            .currency(request.currency())
            .isActive(true)
            .build();
        Page<StockResult> result = stockReader.search(pageable, condition);
        return ApiPageResponse.of(
            result.getContent().stream().map(this::toModelResponse).toList(),
            result.getTotalElements(),
            result.getNumber(),
            result.getSize()
        );
    }

    private StockPayload.ModelResponse toModelResponse(StockResult stock) {
        return new StockPayload.ModelResponse(
            stock.id(),
            stock.ticker(),
            stock.name(),
            stock.exchange(),
            stock.sector(),
            stock.currency(),
            stock.lastPrice(),
            stock.marketCap(),
            stock.isActive(),
            stock.lastSyncedAt()
        );
    }
}
