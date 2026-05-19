package com.revy.example.admin.api.stock;

import com.revy.example.admin.api.common.AbstractCrudApi;
import com.revy.example.admin.api.common.ApiConstants;
import com.revy.example.admin.api.stock.payload.StockPayload;
import com.revy.example.admin.api.stock.usecase.StockUseCase;
import com.revy.example.core.common.ApiPageResponse;
import com.revy.example.core.common.ApiResponse;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(ApiConstants.PREFIX_API_V1 + "/stock")
public class StockController extends
    AbstractCrudApi<Long, StockPayload.CreateRequest, StockPayload.UpdateRequest, StockPayload.SearchRequest, StockPayload.ModelResponse> {

    private final StockUseCase useCase;

    @Override
    protected ApiPageResponse<StockPayload.ModelResponse> getPage(Pageable pageable,
                                                                  StockPayload.SearchRequest searchRequest) {
        return useCase.search(pageable, searchRequest);
    }

    @Override
    protected StockPayload.ModelResponse doCreate(StockPayload.CreateRequest request) {
        return useCase.createStock(request);
    }

    @Override
    protected StockPayload.ModelResponse doGet(Long id) {
        return useCase.get(id);
    }

    @Override
    protected void doDelete(Long id) {
        useCase.delist(id);
    }

    // ── 일반 PATCH 미사용 — market-data 전용 endpoint 사용 ────

    @Hidden
    @Override
    public ResponseEntity<ApiResponse<StockPayload.ModelResponse>> update(Long id, StockPayload.UpdateRequest request) {
        throw new UnsupportedOperationException("Use PATCH /{id}/market-data instead");
    }

    @Override
    protected StockPayload.ModelResponse doUpdate(Long id, StockPayload.UpdateRequest request) {
        throw new UnsupportedOperationException();
    }

    // ── 커스텀 액션 ───────────────────────────────────────────

    @PatchMapping("/{id}/market-data")
    public ResponseEntity<ApiResponse<StockPayload.ModelResponse>> updateMarketData(
            @PathVariable Long id,
            @Valid @RequestBody StockPayload.MarketDataRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(useCase.updateMarketData(id, request)));
    }
}
