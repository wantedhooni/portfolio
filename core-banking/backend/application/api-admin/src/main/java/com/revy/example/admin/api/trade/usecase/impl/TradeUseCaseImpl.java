package com.revy.example.admin.api.trade.usecase.impl;

import com.revy.example.admin.api.trade.payload.TradePayload;
import com.revy.example.admin.api.trade.usecase.TradeUseCase;
import com.revy.example.core.common.ApiPageResponse;
import com.revy.example.core.error.BusinessException;
import com.revy.example.core.error.ErrorCode;
import com.revy.example.domain.account.enums.TxStatus;
import com.revy.example.domain.account.enums.TxType;
import com.revy.example.trade.command.TradeCommand;
import com.revy.example.trade.command.dto.DividendCommand;
import com.revy.example.trade.reader.TradeReader;
import com.revy.example.trade.reader.dto.TradeResult;
import com.revy.example.trade.reader.dto.TradeSearchCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class TradeUseCaseImpl implements TradeUseCase {

    private final TradeReader  tradeReader;
    private final TradeCommand tradeCommand;

    @Override
    public TradePayload.ModelResponse get(Long id) {
        return tradeReader.findById(id)
            .map(this::toResponse)
            .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "체결 내역을 찾을 수 없습니다: " + id));
    }

    @Override
    public ApiPageResponse<TradePayload.ModelResponse> search(Pageable pageable,
                                                               TradePayload.SearchRequest req) {
        TradeSearchCondition condition = TradeSearchCondition.builder()
            .accountId(req.accountId())
            .stockId(req.stockId())
            .txType(parseTxType(req.txType()))
            .status(parseTxStatus(req.status()))
            .referenceId(req.referenceId())
            .tradedAtFrom(req.tradedAtFrom())
            .tradedAtTo(req.tradedAtTo())
            .build();

        Page<TradeResult> page = tradeReader.search(pageable, condition);
        return ApiPageResponse.of(
            page.getContent().stream().map(this::toResponse).toList(),
            page.getTotalElements(), page.getNumber(), page.getSize()
        );
    }

    @Override
    @Transactional
    public void processDividend(TradePayload.DividendRequest req) {
        tradeCommand.dividend(new DividendCommand(
                req.accountId(), req.stockId(),
                req.grossAmount(), req.tax(),
                req.referenceId(), req.tradedAt()
        ));
    }

    // ── private ──────────────────────────────────────────────────

    private TradePayload.ModelResponse toResponse(TradeResult r) {
        return new TradePayload.ModelResponse(
            r.id(), r.accountId(), r.stockId(), r.txType(),
            r.quantity(), r.price(), r.amount(), r.fee(), r.tax(),
            r.status(), r.referenceId(), r.tradedAt()
        );
    }

    private TxType parseTxType(String v) {
        if (v == null || v.isBlank()) return null;
        try { return TxType.valueOf(v.toUpperCase()); } catch (IllegalArgumentException e) { return null; }
    }

    private TxStatus parseTxStatus(String v) {
        if (v == null || v.isBlank()) return null;
        try { return TxStatus.valueOf(v.toUpperCase()); } catch (IllegalArgumentException e) { return null; }
    }
}
