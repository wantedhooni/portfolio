package com.revy.example.saas.api.billing;

import com.revy.example.core.common.ApiPageResponse;
import com.revy.example.core.common.ApiResponse;
import com.revy.example.jwt.payload.JwtPrincipal;
import com.revy.example.saas.api.billing.payload.BillingPayload;
import com.revy.example.saas.api.billing.usecase.BillingUseCase;
import com.revy.example.saas.api.common.ApiConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 사용자(USER) 전용 청구서 API.
 *
 * <p>본인 소유 계좌의 청구서만 조회·납부할 수 있다.
 * 청구서 생성/발행/취소 등 운영 작업은 api-admin 에서만 가능.</p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstants.PREFIX_API_V1 + "/billing/invoices")
public class BillingController {

    private final BillingUseCase useCase;

    @GetMapping
    public ApiResponse<ApiPageResponse<BillingPayload.InvoiceResponse>> myInvoices(
            @AuthenticationPrincipal JwtPrincipal principal,
            Pageable pageable,
            @ModelAttribute BillingPayload.SearchRequest req
    ) {
        return ApiResponse.ok(useCase.myInvoices(principal.id(), pageable, req));
    }

    @GetMapping("/{id}")
    public ApiResponse<BillingPayload.InvoiceResponse> getMyInvoice(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long id
    ) {
        return ApiResponse.ok(useCase.getMyInvoice(principal.id(), id));
    }

    /** 청구서 납부 (ISSUED/OVERDUE → PAID) */
    @PostMapping("/{id}/pay")
    public ApiResponse<Void> pay(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long id
    ) {
        useCase.payMyInvoice(principal.id(), id);
        return ApiResponse.ok();
    }
}
