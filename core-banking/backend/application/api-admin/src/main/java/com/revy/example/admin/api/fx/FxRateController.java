package com.revy.example.admin.api.fx;

import com.revy.example.admin.api.common.ApiConstants;
import com.revy.example.admin.api.fx.payload.FxPayload;
import com.revy.example.admin.api.fx.usecase.FxUseCase;
import com.revy.example.core.common.ApiPageResponse;
import com.revy.example.core.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "FX - ExchangeRate", description = "환율 관리")
@RequiredArgsConstructor
@RestController
@RequestMapping(ApiConstants.PREFIX_API_V1 + "/fx/rate")
public class FxRateController {

    private final FxUseCase useCase;

    @Operation(summary = "환율 등록 (Quote)")
    @PostMapping
    public ResponseEntity<ApiResponse<FxPayload.ExchangeRateResponse>> quote(
            @Valid @RequestBody FxPayload.QuoteRateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ok(useCase.quoteRate(request)));
    }

    @Operation(summary = "환율 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<ApiPageResponse<FxPayload.ExchangeRateResponse>>> search(
            Pageable pageable,
            @Valid @ModelAttribute FxPayload.RateSearchRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(useCase.searchRates(pageable, request)));
    }
}
