package com.revy.example.saas.api.billing.usecase.impl;

import com.revy.example.account.reader.AccountReader;
import com.revy.example.account.reader.dto.AccountResult;
import com.revy.example.billing.command.BillingCommand;
import com.revy.example.billing.reader.BillingReader;
import com.revy.example.billing.reader.dto.BillingInvoiceResult;
import com.revy.example.billing.reader.dto.BillingItemResult;
import com.revy.example.core.common.ApiPageResponse;
import com.revy.example.core.error.BusinessException;
import com.revy.example.core.error.ErrorCode;
import com.revy.example.domain.billing.enums.InvoiceStatus;
import com.revy.example.saas.api.billing.payload.BillingPayload;
import com.revy.example.saas.api.billing.usecase.BillingUseCase;
import com.revy.example.saas.api.common.AccountOwnershipValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class BillingUseCaseImpl implements BillingUseCase {

    private final BillingReader  billingReader;
    private final BillingCommand billingCommand;
    private final AccountReader  accountReader;
    private final AccountOwnershipValidator ownershipValidator;

    @Override
    @Transactional(readOnly = true)
    public ApiPageResponse<BillingPayload.InvoiceResponse> myInvoices(
            Long userId, Pageable pageable, BillingPayload.SearchRequest req) {

        // 본인 소유 계좌 ID 집합
        Set<Long> myAccountIds = accountReader.findAllByUserId(userId).stream()
                .map(AccountResult::id)
                .collect(java.util.stream.Collectors.toSet());

        // 요청에 accountId 가 지정되면 소유권 확인 후 단일 계좌로 제한
        if (req.accountId() != null) {
            if (!myAccountIds.contains(req.accountId())) {
                throw new BusinessException(ErrorCode.FORBIDDEN);
            }
            myAccountIds = Set.of(req.accountId());
        }

        Page<BillingInvoiceResult> page = billingReader.searchByAccountIds(
                pageable, myAccountIds, req.billingPeriod(), toStatus(req.status()));

        return ApiPageResponse.of(
                page.getContent().stream().map(this::toResponse).toList(),
                page.getTotalElements(), page.getNumber(), page.getSize()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public BillingPayload.InvoiceResponse getMyInvoice(Long userId, Long invoiceId) {
        BillingInvoiceResult inv = billingReader.findById(invoiceId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVOICE_NOT_FOUND));
        ownershipValidator.requireOwner(userId, inv.accountId());
        return toResponse(inv);
    }

    @Override
    @Transactional
    public void payMyInvoice(Long userId, Long invoiceId) {
        BillingInvoiceResult inv = billingReader.findById(invoiceId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVOICE_NOT_FOUND));
        ownershipValidator.requireOwner(userId, inv.accountId());
        billingCommand.markPaid(invoiceId);
    }

    // ─────────────────────────────────────────────────────────────

    private BillingPayload.InvoiceResponse toResponse(BillingInvoiceResult r) {
        List<BillingPayload.ItemResponse> items = r.items().stream()
                .map(this::toItem).toList();
        return new BillingPayload.InvoiceResponse(
                r.id(), r.accountId(), r.billingPeriod(), r.status(), r.currency(),
                r.subtotal(), r.taxAmount(), r.totalAmount(),
                r.dueDate(), r.issuedAt(), r.paidAt(), r.note(), items, r.createdAt()
        );
    }

    private BillingPayload.ItemResponse toItem(BillingItemResult i) {
        return new BillingPayload.ItemResponse(
                i.id(), i.type(), i.description(),
                i.quantity(), i.unitPrice(), i.amount()
        );
    }

    private InvoiceStatus toStatus(String s) {
        if (s == null || s.isBlank()) return null;
        try { return InvoiceStatus.valueOf(s.toUpperCase()); }
        catch (IllegalArgumentException e) { throw new BusinessException(ErrorCode.INVALID_INPUT); }
    }
}
