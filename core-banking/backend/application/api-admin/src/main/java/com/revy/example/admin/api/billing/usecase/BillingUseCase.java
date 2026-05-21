package com.revy.example.admin.api.billing.usecase;

import com.revy.example.admin.api.billing.payload.BillingPayload;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

public interface BillingUseCase {
    BillingPayload.ModelResponse get(Long id);
    PageImpl<BillingPayload.ModelResponse> search(Pageable pageable, BillingPayload.SearchRequest req);
    Long createInvoice(BillingPayload.CreateInvoiceRequest req);
    void addItem(Long invoiceId, BillingPayload.AddItemRequest req);
    void issueInvoice(Long invoiceId, BillingPayload.IssueRequest req);
    void markPaid(Long invoiceId);
    void markOverdue(Long invoiceId);
    void cancelInvoice(Long invoiceId);
}
