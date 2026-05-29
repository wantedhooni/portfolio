package com.revy.example.admin.api.pg.usecase;

import com.revy.example.admin.api.pg.payload.PgPayload;
import com.revy.example.core.common.ApiPageResponse;
import org.springframework.data.domain.Pageable;

public interface PgUseCase {

    // ── Merchant ─────────────────────────────────────────────────
    PgPayload.MerchantResponse createMerchant(PgPayload.CreateMerchantRequest request);
    PgPayload.MerchantResponse getMerchant(Long id);
    ApiPageResponse<PgPayload.MerchantResponse> searchMerchants(
            Pageable pageable, PgPayload.MerchantSearchRequest request);
    PgPayload.MerchantResponse updateCommission(Long id, PgPayload.UpdateCommissionRequest request);
    void deactivateMerchant(Long id);
    void activateMerchant(Long id);

    // ── Payment ──────────────────────────────────────────────────
    /** 관리자 데모용: 결제 요청 후 즉시 승인 처리 */
    PgPayload.PaymentResponse requestAndApprovePayment(PgPayload.RequestPaymentRequest request);
    PgPayload.PaymentResponse getPayment(Long id);
    ApiPageResponse<PgPayload.PaymentResponse> searchPayments(
            Pageable pageable, PgPayload.PaymentSearchRequest request);
    void approvePayment(Long id);
    void cancelPayment(Long id);

    // ── PG Settlement ─────────────────────────────────────────────
    PgPayload.SettlementResponse getPgSettlement(Long id);
    ApiPageResponse<PgPayload.SettlementResponse> searchPgSettlements(
            Pageable pageable, PgPayload.SettlementSearchRequest request);
    void failPgSettlement(Long id, PgPayload.FailSettlementRequest request);
}
