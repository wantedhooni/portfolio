package com.revy.example.saas.api.fx;

import com.revy.example.core.common.ApiResponse;
import com.revy.example.core.error.BusinessException;
import com.revy.example.core.error.ErrorCode;
import com.revy.example.domain.fx.enums.RateType;
import com.revy.example.jwt.payload.JwtPrincipal;
import com.revy.example.saas.api.common.ApiConstants;
import com.revy.example.saas.api.fx.payload.FxPayload;
import com.revy.example.saas.api.fx.usecase.FxUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstants.PREFIX_API_V1 + "/fx")
public class FxController {

    private final FxUseCase useCase;

    @GetMapping("/currencies")
    public ApiResponse<List<FxPayload.CurrencyResponse>> listCurrencies() {
        return ApiResponse.ok(useCase.listCurrencies());
    }

    /** 최신 환율 조회 — 환전 화면에서 시세를 미리 보여주기 위한 용도 */
    @GetMapping("/rate/latest")
    public ApiResponse<FxPayload.RateResponse> latestRate(
            @RequestParam String baseCurrencyCode,
            @RequestParam String quoteCurrencyCode,
            @RequestParam(defaultValue = "SELL") RateType rateType
    ) {
        return ApiResponse.ok(
            useCase.latestRate(baseCurrencyCode.toUpperCase(), quoteCurrencyCode.toUpperCase(), rateType)
                   .orElseThrow(() -> new BusinessException(ErrorCode.EXCHANGE_RATE_NOT_FOUND))
        );
    }

    @PostMapping("/conversions")
    public ResponseEntity<ApiResponse<FxPayload.ConversionResponse>> convert(
            @AuthenticationPrincipal JwtPrincipal principal,
            @Valid @RequestBody FxPayload.ConvertRequest request
    ) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.ok(useCase.convert(principal.id(), request)));
    }

    @GetMapping("/conversions/{id}")
    public ApiResponse<FxPayload.ConversionResponse> getConversion(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long id
    ) {
        return ApiResponse.ok(useCase.getConversion(principal.id(), id));
    }
}
