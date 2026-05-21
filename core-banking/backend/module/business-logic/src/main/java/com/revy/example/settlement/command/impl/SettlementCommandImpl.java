package com.revy.example.settlement.command.impl;

import com.revy.example.core.error.BusinessException;
import com.revy.example.core.error.ErrorCode;
import com.revy.example.domain.billing.Settlement;
import com.revy.example.settlement.command.SettlementCommand;
import com.revy.example.settlement.command.dto.CreateSettlementCommand;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@RequiredArgsConstructor
public class SettlementCommandImpl implements SettlementCommand {

    private final EntityManager entityManager;

    @Override
    public Long create(CreateSettlementCommand cmd) {
        Settlement s = Settlement.create(
                cmd.accountId(), cmd.type(), cmd.settlementDate(),
                cmd.currency(), cmd.grossAmount(), cmd.feeAmount(),
                cmd.taxAmount(), cmd.netAmount(),
                cmd.referenceId(), cmd.note()
        );
        entityManager.persist(s);
        return s.getId();
    }

    @Override
    public void settle(Long id) {
        load(id).settle();
    }

    @Override
    public void fail(Long id, String reason) {
        load(id).fail(reason);
    }

    @Override
    public void cancel(Long id) {
        load(id).cancel();
    }

    private Settlement load(Long id) {
        Settlement s = entityManager.find(Settlement.class, id);
        if (s == null) throw new BusinessException(ErrorCode.SETTLEMENT_NOT_FOUND);
        return s;
    }
}
