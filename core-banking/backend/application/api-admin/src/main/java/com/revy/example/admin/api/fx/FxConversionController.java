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

@Slf4j
@Tag(name = "FX - Conversion", description = "환전 실행")
@RequiredArgsConstructor
@RestController
@RequestMapping(ApiConstants.PREFIX_API_V1 + "/fx/conversion")
public class FxConversionController {

    private final FxUseCase useCase;

    @Operation(summary = "환전 실행")
    @PostMapping
    public ResponseEntity<ApiResponse<FxPayload.ConversionResponse>> convert(
            @Valid @RequestBody FxPayload.ConvertRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ok(useCase.convert(request)));
    }

    @Operation(summary = "환전 내역 단건 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FxPayload.ConversionResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(useCase.getConversion(id)));
    }
}
