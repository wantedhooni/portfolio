package com.revy.example.admin.api.settlement.usecase;

import com.revy.example.admin.api.settlement.payload.SettlementPayload;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

public interface SettlementUseCase {
    SettlementPayload.ModelResponse get(Long id);
    PageImpl<SettlementPayload.ModelResponse> search(Pageable pageable, SettlementPayload.SearchRequest req);
    Long create(SettlementPayload.CreateRequest req);
    void settle(Long id);
    void fail(Long id, SettlementPayload.FailRequest req);
    void cancel(Long id);
}
