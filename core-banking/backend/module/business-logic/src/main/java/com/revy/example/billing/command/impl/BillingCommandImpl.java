package com.revy.example.billing.command.impl;

import com.revy.example.billing.command.BillingCommand;
import com.revy.example.billing.command.dto.AddBillingItemCommand;
import com.revy.example.billing.command.dto.CreateInvoiceCommand;
import com.revy.example.core.error.BusinessException;
import com.revy.example.core.error.ErrorCode;
import com.revy.example.domain.billing.BillingInvoice;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;

@Component
@Transactional
@RequiredArgsConstructor
public class BillingCommandImpl implements BillingCommand {

    private final EntityManager entityManager;

    @Override
    public Long createInvoice(CreateInvoiceCommand cmd) {
        BillingInvoice inv = BillingInvoice.create(
                cmd.accountId(), cmd.billingPeriod(), cmd.currency(), cmd.note()
        );
        entityManager.persist(inv);
        return inv.getId();
    }

    @Override
    public void addItem(AddBillingItemCommand cmd) {
        BillingInvoice inv = load(cmd.invoiceId());
        inv.addItem(cmd.type(), cmd.description(), cmd.quantity(), cmd.unitPrice(), cmd.taxRate());
    }

    @Override
    public void issueInvoice(Long invoiceId, LocalDate dueDate) {
        load(invoiceId).issue(dueDate);
    }

    @Override
    public void markPaid(Long invoiceId) {
        load(invoiceId).markPaid(Instant.now());
    }

    @Override
    public void markOverdue(Long invoiceId) {
        load(invoiceId).markOverdue();
    }

    @Override
    public void cancelInvoice(Long invoiceId) {
        load(invoiceId).cancel();
    }

    private BillingInvoice load(Long id) {
        BillingInvoice inv = entityManager.find(BillingInvoice.class, id);
        if (inv == null) throw new BusinessException(ErrorCode.INVOICE_NOT_FOUND);
        return inv;
    }
}
