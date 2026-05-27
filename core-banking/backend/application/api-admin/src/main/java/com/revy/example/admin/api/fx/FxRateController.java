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

import java.util.List;

/**
 * 관리자 환율 API 컨트롤러입니다.
 *
 * <p>현재 환율은 `exchange_rate`, 감사/추적용 이력은 `exchange_rate_history` 기준으로
 * 분리 조회하며, 신규 시세 등록 시 현재 환율과 이력을 함께 갱신합니다.</p>
 */
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

    @Operation(summary = "현재 환율 목록 조회")
    @GetMapping("/current")
    public ResponseEntity<ApiResponse<List<FxPayload.ExchangeRateResponse>>> currentRates() {
        return ResponseEntity.ok(ApiResponse.ok(useCase.listCurrentRates()));
    }

    @Operation(summary = "환율 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<ApiPageResponse<FxPayload.ExchangeRateResponse>>> search(
            Pageable pageable,
            @Valid @ModelAttribute FxPayload.RateSearchRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(useCase.searchRates(pageable, request)));
    }

    @Operation(summary = "환율 이력 목록 조회")
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<ApiPageResponse<FxPayload.ExchangeRateResponse>>> searchHistory(
            Pageable pageable,
            @Valid @ModelAttribute FxPayload.RateSearchRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(useCase.searchRates(pageable, request)));
    }
}
