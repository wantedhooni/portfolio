package com.revy.example.admin.api.billing.usecase.impl;

import com.revy.example.admin.api.billing.payload.BillingPayload;
import com.revy.example.admin.api.billing.usecase.BillingUseCase;
import com.revy.example.billing.command.BillingCommand;
import com.revy.example.billing.command.dto.AddBillingItemCommand;
import com.revy.example.billing.command.dto.CreateInvoiceCommand;
import com.revy.example.billing.reader.BillingReader;
import com.revy.example.billing.reader.dto.BillingInvoiceResult;
import com.revy.example.billing.reader.dto.BillingItemResult;
import com.revy.example.billing.reader.dto.BillingSearchCondition;
import com.revy.example.core.error.BusinessException;
import com.revy.example.core.error.ErrorCode;
import com.revy.example.domain.billing.enums.BillingItemType;
import com.revy.example.domain.billing.enums.InvoiceStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BillingUseCaseImpl implements BillingUseCase {

    private final BillingCommand billingCommand;
    private final BillingReader  billingReader;

    @Override
    public BillingPayload.ModelResponse get(Long id) {
        return map(billingReader.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVOICE_NOT_FOUND)));
    }

    @Override
    public PageImpl<BillingPayload.ModelResponse> search(Pageable pageable, BillingPayload.SearchRequest req) {
        BillingSearchCondition cond = new BillingSearchCondition(
                req.accountId(), req.billingPeriod(), toStatus(req.status())
        );
        Page<BillingInvoiceResult> page = billingReader.search(pageable, cond);
        return new PageImpl<>(page.getContent().stream().map(this::map).toList(), pageable, page.getTotalElements());
    }

    @Override
    @Transactional
    public Long createInvoice(BillingPayload.CreateInvoiceRequest req) {
        return billingCommand.createInvoice(new CreateInvoiceCommand(
                req.accountId(), req.billingPeriod(), req.currency(), req.note()
        ));
    }

    @Override
    @Transactional
    public void addItem(Long invoiceId, BillingPayload.AddItemRequest req) {
        billingCommand.addItem(new AddBillingItemCommand(
                invoiceId, toItemType(req.type()),
                req.description(), req.quantity(), req.unitPrice(), req.taxRate()
        ));
    }

    @Override @Transactional public void issueInvoice(Long id, BillingPayload.IssueRequest req) { billingCommand.issueInvoice(id, req.dueDate()); }
    @Override @Transactional public void markPaid(Long id)    { billingCommand.markPaid(id); }
    @Override @Transactional public void markOverdue(Long id) { billingCommand.markOverdue(id); }
    @Override @Transactional public void cancelInvoice(Long id) { billingCommand.cancelInvoice(id); }

    private BillingPayload.ModelResponse map(BillingInvoiceResult r) {
        List<BillingPayload.ItemResponse> items = r.items().stream()
                .map(i -> new BillingPayload.ItemResponse(
                        i.id(), i.type(), i.description(), i.quantity(), i.unitPrice(), i.amount()))
                .toList();
        return new BillingPayload.ModelResponse(
                r.id(), r.accountId(), r.billingPeriod(), r.status(), r.currency(),
                r.subtotal(), r.taxAmount(), r.totalAmount(),
                r.dueDate(), r.issuedAt(), r.paidAt(), r.note(), items, r.createdAt()
        );
    }

    private BillingItemType toItemType(String s) {
        try { return BillingItemType.valueOf(s.toUpperCase()); }
        catch (IllegalArgumentException e) { throw new BusinessException(ErrorCode.INVALID_INPUT); }
    }

    private InvoiceStatus toStatus(String s) {
        if (s == null || s.isBlank()) return null;
        try { return InvoiceStatus.valueOf(s.toUpperCase()); }
        catch (IllegalArgumentException e) { throw new BusinessException(ErrorCode.INVALID_INPUT); }
    }
}
