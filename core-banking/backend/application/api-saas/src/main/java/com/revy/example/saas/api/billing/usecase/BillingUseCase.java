package com.revy.example.saas.api.billing.usecase;

import com.revy.example.core.common.ApiPageResponse;
import com.revy.example.saas.api.billing.payload.BillingPayload;
import org.springframework.data.domain.Pageable;

public interface BillingUseCase {

    /** 본인 소유 계좌의 청구서 목록 (페이지) */
    ApiPageResponse<BillingPayload.InvoiceResponse> myInvoices(
            Long userId, Pageable pageable, BillingPayload.SearchRequest req);

    /** 본인 소유 청구서 단건 (다른 사용자의 청구서면 FORBIDDEN) */
    BillingPayload.InvoiceResponse getMyInvoice(Long userId, Long invoiceId);

    /** 본인 청구서 납부 처리 (ISSUED/OVERDUE → PAID) */
    void payMyInvoice(Long userId, Long invoiceId);
}
