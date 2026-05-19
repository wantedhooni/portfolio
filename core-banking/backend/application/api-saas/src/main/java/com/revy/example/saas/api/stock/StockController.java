package com.revy.example.saas.api.stock;

import com.revy.example.core.common.ApiPageResponse;
import com.revy.example.core.common.ApiResponse;
import com.revy.example.saas.api.common.ApiConstants;
import com.revy.example.saas.api.stock.payload.StockPayload;
import com.revy.example.saas.api.stock.usecase.StockUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstants.PREFIX_API_V1 + "/stocks")
public class StockController {

    private final StockUseCase useCase;

    @GetMapping
    public ApiResponse<ApiPageResponse<StockPayload.ModelResponse>> search(
            Pageable pageable,
            @Valid @ModelAttribute StockPayload.SearchRequest request
    ) {
        return ApiResponse.ok(useCase.search(pageable, request));
    }

    @GetMapping("/{stockId}")
    public ApiResponse<StockPayload.ModelResponse> get(@PathVariable Long stockId) {
        return ApiResponse.ok(useCase.get(stockId));
    }
}
