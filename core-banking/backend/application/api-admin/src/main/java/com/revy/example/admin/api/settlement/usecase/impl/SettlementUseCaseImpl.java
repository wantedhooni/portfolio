package com.revy.example.admin.api.settlement.usecase.impl;

import com.revy.example.admin.api.settlement.payload.SettlementPayload;
import com.revy.example.admin.api.settlement.usecase.SettlementUseCase;
import com.revy.example.core.error.BusinessException;
import com.revy.example.core.error.ErrorCode;
import com.revy.example.domain.billing.enums.SettlementStatus;
import com.revy.example.domain.billing.enums.SettlementType;
import com.revy.example.settlement.command.SettlementCommand;
import com.revy.example.settlement.command.dto.CreateSettlementCommand;
import com.revy.example.settlement.reader.SettlementReader;
import com.revy.example.settlement.reader.dto.SettlementResult;
import com.revy.example.settlement.reader.dto.SettlementSearchCondition;
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
public class SettlementUseCaseImpl implements SettlementUseCase {

    private final SettlementCommand settlementCommand;
    private final SettlementReader  settlementReader;

    @Override
    public SettlementPayload.ModelResponse get(Long id) {
        return map(settlementReader.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.SETTLEMENT_NOT_FOUND)));
    }

    @Override
    public PageImpl<SettlementPayload.ModelResponse> search(Pageable pageable, SettlementPayload.SearchRequest req) {
        SettlementSearchCondition condition = new SettlementSearchCondition(
                req.accountId(), toType(req.type()), toStatus(req.status()),
                req.settlementDateFrom(), req.settlementDateTo()
        );
        Page<SettlementResult> page = settlementReader.search(pageable, condition);
        List<SettlementPayload.ModelResponse> content = page.getContent().stream().map(this::map).toList();
        return new PageImpl<>(content, pageable, page.getTotalElements());
    }

    @Override
    @Transactional
    public Long create(SettlementPayload.CreateRequest req) {
        return settlementCommand.create(new CreateSettlementCommand(
                req.accountId(), toType(req.type()), req.settlementDate(),
                req.currency(), req.grossAmount(), req.feeAmount(),
                req.taxAmount(), req.netAmount(), req.referenceId(), req.note()
        ));
    }

    @Override @Transactional public void settle(Long id) { settlementCommand.settle(id); }
    @Override @Transactional public void fail(Long id, SettlementPayload.FailRequest req) { settlementCommand.fail(id, req.reason()); }
    @Override @Transactional public void cancel(Long id) { settlementCommand.cancel(id); }

    private SettlementPayload.ModelResponse map(SettlementResult r) {
        return new SettlementPayload.ModelResponse(
                r.id(), r.accountId(), r.type(), r.settlementDate(), r.status(),
                r.currency(), r.grossAmount(), r.feeAmount(), r.taxAmount(), r.netAmount(),
                r.referenceId(), r.note(), r.failedReason(), r.settledAt(), r.createdAt()
        );
    }

    private SettlementType toType(String s) {
        if (s == null || s.isBlank()) return null;
        try { return SettlementType.valueOf(s.toUpperCase()); }
        catch (IllegalArgumentException e) { throw new BusinessException(ErrorCode.INVALID_INPUT); }
    }

    private SettlementStatus toStatus(String s) {
        if (s == null || s.isBlank()) return null;
        try { return SettlementStatus.valueOf(s.toUpperCase()); }
        catch (IllegalArgumentException e) { throw new BusinessException(ErrorCode.INVALID_INPUT); }
    }
}
