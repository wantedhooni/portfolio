package com.revy.example.admin.api.pg.usecase.impl;

import com.revy.example.admin.api.pg.payload.PgPayload;
import com.revy.example.admin.api.pg.usecase.PgUseCase;
import com.revy.example.core.common.ApiPageResponse;
import com.revy.example.core.error.BusinessException;
import com.revy.example.core.error.ErrorCode;
import com.revy.example.pg.command.PgCommand;
import com.revy.example.pg.command.dto.CreatePgMerchantCommand;
import com.revy.example.pg.command.dto.CreatePgPaymentCommand;
import com.revy.example.pg.reader.PgReader;
import com.revy.example.pg.reader.dto.PgMerchantResult;
import com.revy.example.pg.reader.dto.PgPaymentResult;
import com.revy.example.pg.reader.dto.PgSettlementResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PgUseCaseImpl implements PgUseCase {

    private final PgCommand pgCommand;
    private final PgReader  pgReader;

    // ── Merchant ─────────────────────────────────────────────────

    @Override
    @Transactional
    public PgPayload.MerchantResponse createMerchant(PgPayload.CreateMerchantRequest req) {
        if (pgReader.existsMerchantByCode(req.merchantCode())) {
            throw new BusinessException(ErrorCode.PG_MERCHANT_DUPLICATED);
        }
        Long id = pgCommand.createMerchant(new CreatePgMerchantCommand(
                req.merchantCode(), req.name(), req.businessType(),
                req.settlementAccountId(), req.commissionRate(),
                req.settlementCycle(), req.currency(), req.contactEmail()
        ));
        return getMerchant(id);
    }

    @Override
    public PgPayload.MerchantResponse getMerchant(Long id) {
        return pgReader.findMerchantById(id)
                .map(this::toMerchantResponse)
                .orElseThrow(() -> new BusinessException(ErrorCode.PG_MERCHANT_NOT_FOUND));
    }

    @Override
    public ApiPageResponse<PgPayload.MerchantResponse> searchMerchants(
            Pageable pageable, PgPayload.MerchantSearchRequest req) {
        List<PgMerchantResult> all = pgReader.findAllActiveMerchants();
        List<PgPayload.MerchantResponse> filtered = all.stream()
                .filter(m -> isBlank(req.merchantCode())
                        || m.merchantCode().contains(req.merchantCode()))
                .filter(m -> isBlank(req.businessType())
                        || m.businessType().equalsIgnoreCase(req.businessType()))
                .filter(m -> isBlank(req.isActive())
                        || Boolean.toString(m.isActive()).equalsIgnoreCase(req.isActive()))
                .map(this::toMerchantResponse)
                .toList();

        int total = filtered.size();
        int from  = (int) Math.min(pageable.getOffset(), total);
        int to    = Math.min(from + pageable.getPageSize(), total);
        return ApiPageResponse.of(filtered.subList(from, to), total,
                pageable.getPageNumber(), pageable.getPageSize());
    }

    @Override
    @Transactional
    public PgPayload.MerchantResponse updateCommission(Long id, PgPayload.UpdateCommissionRequest req) {
        pgCommand.updateCommissionRate(id, req.commissionRate());
        return getMerchant(id);
    }

    @Override @Transactional
    public void deactivateMerchant(Long id) { pgCommand.deactivateMerchant(id); }

    @Override @Transactional
    public void activateMerchant(Long id)   { pgCommand.activateMerchant(id); }

    // ── Payment ──────────────────────────────────────────────────

    @Override
    @Transactional
    public PgPayload.PaymentResponse requestAndApprovePayment(PgPayload.RequestPaymentRequest req) {
        if (pgReader.existsPaymentByOrderNo(req.orderNo())) {
            throw new BusinessException(ErrorCode.PG_PAYMENT_DUPLICATED);
        }
        Long paymentId = pgCommand.requestPayment(new CreatePgPaymentCommand(
                req.merchantId(), req.paymentMethod(), req.orderNo(),
                req.amount(), req.currency()
        ));
        pgCommand.approvePayment(paymentId);
        return getPayment(paymentId);
    }

    @Override
    public PgPayload.PaymentResponse getPayment(Long id) {
        return pgReader.findPaymentById(id)
                .map(this::toPaymentResponse)
                .orElseThrow(() -> new BusinessException(ErrorCode.PG_PAYMENT_NOT_FOUND));
    }

    @Override
    public ApiPageResponse<PgPayload.PaymentResponse> searchPayments(
            Pageable pageable, PgPayload.PaymentSearchRequest req) {
        Page<PgPaymentResult> page = pgReader.searchPayments(pageable, req.merchantId());
        List<PgPayload.PaymentResponse> content = page.getContent().stream()
                .filter(p -> isBlank(req.paymentMethod())
                        || p.paymentMethod().name().equalsIgnoreCase(req.paymentMethod()))
                .filter(p -> isBlank(req.status())
                        || p.status().name().equalsIgnoreCase(req.status()))
                .map(this::toPaymentResponse)
                .toList();
        return ApiPageResponse.of(content, page.getTotalElements(),
                pageable.getPageNumber(), pageable.getPageSize());
    }

    @Override @Transactional
    public void approvePayment(Long id) { pgCommand.approvePayment(id); }

    @Override @Transactional
    public void cancelPayment(Long id)  { pgCommand.cancelPayment(id); }

    // ── PG Settlement ─────────────────────────────────────────────

    @Override
    public PgPayload.SettlementResponse getPgSettlement(Long id) {
        return pgReader.findSettlementById(id)
                .map(this::toSettlementResponse)
                .orElseThrow(() -> new BusinessException(ErrorCode.PG_SETTLEMENT_NOT_FOUND));
    }

    @Override
    public ApiPageResponse<PgPayload.SettlementResponse> searchPgSettlements(
            Pageable pageable, PgPayload.SettlementSearchRequest req) {
        Page<PgSettlementResult> page = pgReader.searchSettlements(pageable, req.merchantId());
        List<PgPayload.SettlementResponse> content = page.getContent().stream()
                .filter(s -> isBlank(req.status())
                        || s.status().name().equalsIgnoreCase(req.status()))
                .filter(s -> isBlank(req.settlementDateFrom())
                        || s.settlementDate().toString().compareTo(req.settlementDateFrom()) >= 0)
                .filter(s -> isBlank(req.settlementDateTo())
                        || s.settlementDate().toString().compareTo(req.settlementDateTo()) <= 0)
                .map(this::toSettlementResponse)
                .toList();
        return ApiPageResponse.of(content, page.getTotalElements(),
                pageable.getPageNumber(), pageable.getPageSize());
    }

    @Override @Transactional
    public void failPgSettlement(Long id, PgPayload.FailSettlementRequest req) {
        pgCommand.failSettlement(id, req.reason());
    }

    // ── 내부 ─────────────────────────────────────────────────────

    private PgPayload.MerchantResponse toMerchantResponse(PgMerchantResult r) {
        return new PgPayload.MerchantResponse(
                r.id(), r.merchantCode(), r.name(), r.businessType(),
                r.settlementAccountId(), r.commissionRate(), r.settlementCycle(),
                r.currency(), r.isActive(), r.contactEmail(), r.createdAt()
        );
    }

    private PgPayload.PaymentResponse toPaymentResponse(PgPaymentResult r) {
        return new PgPayload.PaymentResponse(
                r.id(), r.merchantId(), r.paymentMethod().name(), r.orderNo(),
                r.amount(), r.commissionAmount(), r.netAmount(), r.currency(),
                r.status(), r.requestedAt(), r.approvedAt(), null,
                r.pgSettlementId(), Instant.now()
        );
    }

    private PgPayload.SettlementResponse toSettlementResponse(PgSettlementResult r) {
        return new PgPayload.SettlementResponse(
                r.id(), r.merchantId(), r.targetDate(), r.settlementDate(),
                r.paymentCount(), r.totalAmount(), r.commissionAmount(), r.netAmount(),
                r.currency(), r.status(), r.settledAt(), r.failedReason(),
                r.referenceId(), r.createdAt()
        );
    }

    private static boolean isBlank(String s) { return s == null || s.isBlank(); }
}
