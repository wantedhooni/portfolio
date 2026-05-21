package com.revy.example.domain.billing.exception;

import com.revy.example.core.error.ErrorCode;

public class InvoiceNotFoundException extends BillingException {
    public InvoiceNotFoundException() { super(ErrorCode.INVOICE_NOT_FOUND); }
}
