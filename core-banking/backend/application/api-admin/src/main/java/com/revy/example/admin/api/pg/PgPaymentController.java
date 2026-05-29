package com.revy.example.admin.api.pg;

import com.revy.example.admin.api.common.ApiConstants;
import com.revy.example.admin.api.pg.payload.PgPayload;
import com.revy.example.admin.api.pg.usecase.PgUseCase;
import com.revy.example.core.common.ApiPageResponse;
import com.revy.example.core.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "PG - Payment", description = "PG 결제 관리")
@RestController
@RequestMapping(ApiConstants.PREFIX_API_V1 + "/pg/payment")
@RequiredArgsConstructor
public class PgPaymentController {

    private final PgUseCase useCase;

    @Operation(summary = "결제 목록 조회")
    @GetMapping
    public ApiPageResponse<PgPayload.PaymentResponse> search(
            Pageable pageable,
            @ModelAttribute PgPayload.PaymentSearchRequest req) {
        return useCase.searchPayments(pageable, req);
    }

    @Operation(summary = "결제 단건 조회")
    @GetMapping("/{id}")
    public ApiResponse<PgPayload.PaymentResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(useCase.getPayment(id));
    }

    @Operation(summary = "결제 요청 + 즉시 승인 (데모용)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PgPayload.PaymentResponse> requestAndApprove(
            @RequestBody @Valid PgPayload.RequestPaymentRequest req) {
        return ApiResponse.ok(useCase.requestAndApprovePayment(req));
    }

    @Operation(summary = "결제 승인")
    @PostMapping("/{id}/approve")
    public ApiResponse<Void> approve(@PathVariable Long id) {
        useCase.approvePayment(id);
        return ApiResponse.ok();
    }

    @Operation(summary = "결제 취소")
    @PostMapping("/{id}/cancel")
    public ApiResponse<Void> cancel(@PathVariable Long id) {
        useCase.cancelPayment(id);
        return ApiResponse.ok();
    }
}
