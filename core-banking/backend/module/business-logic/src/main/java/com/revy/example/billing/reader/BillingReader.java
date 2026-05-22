package com.revy.example.billing.reader;

import com.revy.example.billing.reader.dto.BillingInvoiceResult;
import com.revy.example.billing.reader.dto.BillingSearchCondition;
import com.revy.example.domain.billing.enums.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.Optional;

public interface BillingReader {

    Optional<BillingInvoiceResult> findById(Long id);

    Page<BillingInvoiceResult> search(Pageable pageable, BillingSearchCondition condition);

    /** 사용자 소유 계좌들의 청구서를 한 번에 검색 (api-saas user-scoped 조회) */
    Page<BillingInvoiceResult> searchByAccountIds(Pageable pageable,
                                                  Collection<Long> accountIds,
                                                  String billingPeriod,
                                                  InvoiceStatus status);
}
