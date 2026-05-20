package com.revy.example.admin.api.fx;

import com.revy.example.admin.api.common.ApiConstants;
import com.revy.example.admin.api.fx.payload.FxPayload;
import com.revy.example.admin.api.fx.usecase.FxUseCase;
import com.revy.example.core.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@Tag(name = "FX - Currency", description = "통화 관리")
@RequiredArgsConstructor
@RestController
@RequestMapping(ApiConstants.PREFIX_API_V1 + "/fx/currency")
public class FxCurrencyController {

    private final FxUseCase useCase;

    @Operation(summary = "통화 등록")
    @PostMapping
    public ResponseEntity<ApiResponse<FxPayload.CurrencyResponse>> register(
            @Valid @RequestBody FxPayload.RegisterCurrencyRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ok(useCase.registerCurrency(request)));
    }

    @Operation(summary = "통화 단건 조회")
    @GetMapping("/{code}")
    public ResponseEntity<ApiResponse<FxPayload.CurrencyResponse>> get(@PathVariable String code) {
        return ResponseEntity.ok(ApiResponse.ok(useCase.getCurrency(code)));
    }

    @Operation(summary = "활성 통화 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<List<FxPayload.CurrencyResponse>>> listActive() {
        return ResponseEntity.ok(ApiResponse.ok(useCase.listActiveCurrencies()));
    }

    @Operation(summary = "통화 활성화")
    @PostMapping("/{code}/activate")
    public ResponseEntity<ApiResponse<Void>> activate(@PathVariable String code) {
        useCase.activateCurrency(code);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @Operation(summary = "통화 비활성화")
    @PostMapping("/{code}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable String code) {
        useCase.deactivateCurrency(code);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
