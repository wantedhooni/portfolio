package com.revy.example.billing.reader;

import com.revy.example.billing.reader.dto.BillingInvoiceResult;
import com.revy.example.billing.reader.dto.BillingSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface BillingReader {

    Optional<BillingInvoiceResult> findById(Long id);

    Page<BillingInvoiceResult> search(Pageable pageable, BillingSearchCondition condition);
}
