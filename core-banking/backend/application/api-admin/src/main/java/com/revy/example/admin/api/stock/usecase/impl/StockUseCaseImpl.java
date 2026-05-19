package com.revy.example.admin.api.stock.usecase.impl;

import com.revy.example.admin.api.stock.payload.StockPayload;
import com.revy.example.admin.api.stock.usecase.StockUseCase;
import com.revy.example.core.common.ApiPageResponse;
import com.revy.example.domain.account.exception.StockNotFoundException;
import com.revy.example.stock.command.StockCommand;
import com.revy.example.stock.command.dto.CreateStockCommand;
import com.revy.example.stock.command.dto.UpdateMarketDataCommand;
import com.revy.example.stock.reader.StockReader;
import com.revy.example.stock.reader.dto.StockResult;
import com.revy.example.stock.reader.dto.StockSearchCondition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockUseCaseImpl implements StockUseCase {

    private final StockReader  stockReader;
    private final StockCommand stockCommand;

    @Override
    public StockPayload.ModelResponse createStock(StockPayload.CreateRequest request) {
        Long id = stockCommand.createStock(new CreateStockCommand(
            request.ticker(),
            request.name(),
            request.exchange(),
            request.sector(),
            request.currency()
        ));
        return get(id);
    }

    @Override
    public StockPayload.ModelResponse get(Long id) {
        return stockReader.findById(id)
            .map(this::toModelResponse)
            .orElseThrow(StockNotFoundException::new);
    }

    @Override
    public ApiPageResponse<StockPayload.ModelResponse> search(Pageable pageable, StockPayload.SearchRequest searchRequest) {
        StockSearchCondition condition = StockSearchCondition.builder()
            .ticker(searchRequest.ticker())
            .name(searchRequest.name())
            .exchange(searchRequest.exchange())
            .sector(searchRequest.sector())
            .currency(searchRequest.currency())
            .isActive(searchRequest.isActive())
            .build();
        Page<StockResult> result = stockReader.search(pageable, condition);
        return ApiPageResponse.of(
            result.getContent().stream().map(this::toModelResponse).toList(),
            result.getTotalElements(),
            result.getNumber(),
            result.getSize()
        );
    }

    @Override
    public StockPayload.ModelResponse updateMarketData(Long id, StockPayload.MarketDataRequest request) {
        stockCommand.updateMarketData(new UpdateMarketDataCommand(id, request.lastPrice(), request.marketCap()));
        return get(id);
    }

    @Override
    public void delist(Long id) {
        stockCommand.delist(id);
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
