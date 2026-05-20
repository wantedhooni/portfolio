package com.revy.example.admin.api.ledger;

import com.revy.example.admin.api.common.ApiConstants;
import com.revy.example.admin.api.ledger.payload.LedgerPayload;
import com.revy.example.admin.api.ledger.usecase.LedgerUseCase;
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
@Tag(name = "Ledger - Period", description = "회계기간 관리")
@RequiredArgsConstructor
@RestController
@RequestMapping(ApiConstants.PREFIX_API_V1 + "/ledger/period")
public class LedgerPeriodController {

    private final LedgerUseCase useCase;

    @Operation(summary = "회계기간 개설")
    @PostMapping
    public ResponseEntity<ApiResponse<LedgerPayload.PeriodResponse>> open(
            @Valid @RequestBody LedgerPayload.OpenPeriodRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ok(useCase.openPeriod(request)));
    }

    @Operation(summary = "회계기간 단건 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LedgerPayload.PeriodResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(useCase.getPeriod(id)));
    }

    @Operation(summary = "회계기간 마감")
    @PostMapping("/{id}/close")
    public ResponseEntity<ApiResponse<Void>> close(
            @PathVariable Long id,
            @Valid @RequestBody LedgerPayload.ClosePeriodRequest request
    ) {
        useCase.closePeriod(id, request);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
