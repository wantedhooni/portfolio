package com.revy.example.billing.command;

import com.revy.example.billing.command.dto.AddBillingItemCommand;
import com.revy.example.billing.command.dto.CreateInvoiceCommand;

import java.time.LocalDate;

public interface BillingCommand {

    Long createInvoice(CreateInvoiceCommand command);

    void addItem(AddBillingItemCommand command);

    void issueInvoice(Long invoiceId, LocalDate dueDate);

    void markPaid(Long invoiceId);

    void markOverdue(Long invoiceId);

    void cancelInvoice(Long invoiceId);
}
