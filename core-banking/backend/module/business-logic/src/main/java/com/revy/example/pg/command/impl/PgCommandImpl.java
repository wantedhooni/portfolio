package com.revy.example.pg.command.impl;

import com.revy.example.core.error.BusinessException;
import com.revy.example.core.error.ErrorCode;
import com.revy.example.domain.pg.PgMerchant;
import com.revy.example.domain.pg.PgPayment;
import com.revy.example.domain.pg.PgSettlement;
import com.revy.example.domain.pg.exception.PgMerchantNotFoundException;
import com.revy.example.domain.pg.exception.PgPaymentNotFoundException;
import com.revy.example.domain.pg.exception.PgSettlementNotFoundException;
import com.revy.example.pg.command.PgCommand;
import com.revy.example.pg.command.dto.CreatePgMerchantCommand;
import com.revy.example.pg.command.dto.CreatePgPaymentCommand;
import com.revy.example.pg.command.dto.CreatePgSettlementCommand;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class PgCommandImpl implements PgCommand {

    private final EntityManager entityManager;

    // ── Merchant ─────────────────────────────────────────────────

    @Override
    public Long createMerchant(CreatePgMerchantCommand cmd) {
        PgMerchant merchant = PgMerchant.create(
                cmd.merchantCode(), cmd.name(), cmd.businessType(),
                cmd.settlementAccountId(), cmd.commissionRate(),
                cmd.settlementCycle(), cmd.currency(), cmd.contactEmail()
        );
        entityManager.persist(merchant);
        return merchant.getId();
    }

    @Override
    public void updateCommissionRate(Long merchantId, java.math.BigDecimal commissionRate) {
        loadMerchant(merchantId).updateCommissionRate(commissionRate);
    }

    @Override
    public void deactivateMerchant(Long merchantId) {
        loadMerchant(merchantId).deactivate();
    }

    @Override
    public void activateMerchant(Long merchantId) {
        loadMerchant(merchantId).activate();
    }

    // ── Payment ──────────────────────────────────────────────────

    @Override
    public Long requestPayment(CreatePgPaymentCommand cmd) {
        PgMerchant merchant = loadMerchant(cmd.merchantId());
        merchant.validateActive();

        BigDecimal amount           = cmd.amount();
        BigDecimal commissionAmount = amount.multiply(merchant.getCommissionRate())
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal netAmount        = amount.subtract(commissionAmount);

        PgPayment payment = PgPayment.request(
                cmd.merchantId(), cmd.paymentMethod(), cmd.orderNo(),
                amount, commissionAmount, netAmount, cmd.currency()
        );
        entityManager.persist(payment);
        return payment.getId();
    }

    @Override
    public void approvePayment(Long paymentId) {
        loadPayment(paymentId).approve();
    }

    @Override
    public void cancelPayment(Long paymentId) {
        loadPayment(paymentId).cancel();
    }

    // ── Settlement ───────────────────────────────────────────────

    @Override
    public Long createSettlement(CreatePgSettlementCommand cmd) {
        PgSettlement settlement = PgSettlement.create(
                cmd.merchantId(), cmd.targetDate(), cmd.settlementDate(),
                cmd.paymentCount(), cmd.totalAmount(), cmd.commissionAmount(),
                cmd.netAmount(), cmd.currency(), cmd.referenceId()
        );
        entityManager.persist(settlement);
        return settlement.getId();
    }

    @Override
    public void completeSettlement(Long settlementId, List<Long> paymentIds) {
        PgSettlement settlement = loadSettlement(settlementId);
        settlement.complete();

        // 포함 결제의 pgSettlementId 연결
        for (Long pid : paymentIds) {
            PgPayment payment = entityManager.find(PgPayment.class, pid);
            if (payment != null) {
                payment.linkToSettlement(settlementId);
            } else {
                log.warn("[PgCommand] 결제를 찾을 수 없어 연결 스킵 paymentId={}", pid);
            }
        }

        log.info("[PgCommand] 정산 완료 settlementId={} paymentCount={}",
                settlementId, paymentIds.size());
    }

    @Override
    public void failSettlement(Long settlementId, String reason) {
        loadSettlement(settlementId).fail(reason);
    }

    // ── 내부 ─────────────────────────────────────────────────────

    private PgMerchant loadMerchant(Long id) {
        PgMerchant m = entityManager.find(PgMerchant.class, id);
        if (m == null) throw new PgMerchantNotFoundException();
        return m;
    }

    private PgPayment loadPayment(Long id) {
        PgPayment p = entityManager.find(PgPayment.class, id);
        if (p == null) throw new PgPaymentNotFoundException();
        return p;
    }

    private PgSettlement loadSettlement(Long id) {
        PgSettlement s = entityManager.find(PgSettlement.class, id);
        if (s == null) throw new PgSettlementNotFoundException();
        return s;
    }
}
