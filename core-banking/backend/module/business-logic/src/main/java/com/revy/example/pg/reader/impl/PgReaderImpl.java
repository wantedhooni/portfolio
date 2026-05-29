package com.revy.example.pg.reader.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.revy.example.domain.pg.PgMerchant;
import com.revy.example.domain.pg.PgPayment;
import com.revy.example.domain.pg.PgSettlement;
import com.revy.example.domain.pg.QPgMerchant;
import com.revy.example.domain.pg.QPgPayment;
import com.revy.example.domain.pg.QPgSettlement;
import com.revy.example.domain.pg.enums.PgPaymentStatus;
import com.revy.example.domain.pg.enums.PgSettlementStatus;
import com.revy.example.domain.ledger.QJournalEntry;
import com.revy.example.pg.reader.PgReader;
import com.revy.example.pg.reader.dto.PgMerchantResult;
import com.revy.example.pg.reader.dto.PgPaymentResult;
import com.revy.example.pg.reader.dto.PgSettlementResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@Component
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PgReaderImpl implements PgReader {

    private final JPAQueryFactory jpaQueryFactory;

    private final QPgMerchant    M  = QPgMerchant.pgMerchant;
    private final QPgPayment     P  = QPgPayment.pgPayment;
    private final QPgSettlement  S  = QPgSettlement.pgSettlement;
    private final QJournalEntry  JE = QJournalEntry.journalEntry;

    private static final String PG_LEDGER_REF_TYPE = "PG_SETTLEMENT";

    // ── Merchant ─────────────────────────────────────────────────

    @Override
    public Optional<PgMerchantResult> findMerchantById(Long id) {
        return Optional.ofNullable(jpaQueryFactory.selectFrom(M).where(M.id.eq(id)).fetchOne())
                .map(PgMerchantResult::from);
    }

    @Override
    public Optional<PgMerchantResult> findMerchantByCode(String merchantCode) {
        return Optional.ofNullable(
                jpaQueryFactory.selectFrom(M).where(M.merchantCode.eq(merchantCode)).fetchOne())
                .map(PgMerchantResult::from);
    }

    @Override
    public boolean existsMerchantByCode(String merchantCode) {
        return jpaQueryFactory.selectOne().from(M)
                .where(M.merchantCode.eq(merchantCode)).fetchFirst() != null;
    }

    @Override
    public List<PgMerchantResult> findAllActiveMerchants() {
        return jpaQueryFactory.selectFrom(M)
                .where(M.isActive.isTrue())
                .orderBy(M.id.asc())
                .fetch()
                .stream().map(PgMerchantResult::from).toList();
    }

    // ── Payment ──────────────────────────────────────────────────

    @Override
    public Optional<PgPaymentResult> findPaymentById(Long id) {
        return Optional.ofNullable(jpaQueryFactory.selectFrom(P).where(P.id.eq(id)).fetchOne())
                .map(PgPaymentResult::from);
    }

    @Override
    public boolean existsPaymentByOrderNo(String orderNo) {
        return jpaQueryFactory.selectOne().from(P)
                .where(P.orderNo.eq(orderNo)).fetchFirst() != null;
    }

    @Override
    public Page<PgPaymentResult> searchPayments(Pageable pageable, Long merchantId) {
        BooleanBuilder where = new BooleanBuilder();
        if (merchantId != null) where.and(P.merchantId.eq(merchantId));

        List<PgPaymentResult> content = jpaQueryFactory.selectFrom(P)
                .where(where)
                .orderBy(P.approvedAt.desc().nullsLast(), P.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch().stream().map(PgPaymentResult::from).toList();

        JPAQuery<Long> countQuery = jpaQueryFactory.select(P.count()).from(P).where(where);
        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public List<PgPaymentResult> findApprovedPaymentsForSettlement(Long merchantId, LocalDate targetDate) {
        Instant from = targetDate.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant to   = targetDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        return jpaQueryFactory.selectFrom(P)
                .where(
                        P.merchantId.eq(merchantId),
                        P.status.eq(PgPaymentStatus.APPROVED),
                        P.approvedAt.goe(from),
                        P.approvedAt.lt(to),
                        P.pgSettlementId.isNull()
                )
                .orderBy(P.id.asc())
                .fetch()
                .stream().map(PgPaymentResult::from).toList();
    }

    // ── Settlement ───────────────────────────────────────────────

    @Override
    public Optional<PgSettlementResult> findSettlementById(Long id) {
        return Optional.ofNullable(jpaQueryFactory.selectFrom(S).where(S.id.eq(id)).fetchOne())
                .map(PgSettlementResult::from);
    }

    @Override
    public boolean existsSettlementByReferenceId(String referenceId) {
        return jpaQueryFactory.selectOne().from(S)
                .where(S.referenceId.eq(referenceId)).fetchFirst() != null;
    }

    @Override
    public Page<PgSettlementResult> searchSettlements(Pageable pageable, Long merchantId) {
        BooleanBuilder where = new BooleanBuilder();
        if (merchantId != null) where.and(S.merchantId.eq(merchantId));

        List<PgSettlementResult> content = jpaQueryFactory.selectFrom(S)
                .where(where)
                .orderBy(S.settlementDate.desc(), S.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch().stream().map(PgSettlementResult::from).toList();

        JPAQuery<Long> countQuery = jpaQueryFactory.select(S.count()).from(S).where(where);
        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public List<PgSettlementResult> findSettledForLedger(LocalDate settlementDate) {
        return jpaQueryFactory.selectFrom(S)
                .where(
                        S.status.eq(PgSettlementStatus.SETTLED),
                        S.settlementDate.eq(settlementDate),
                        jpaQueryFactory.selectOne().from(JE)
                                .where(
                                        JE.referenceType.eq(PG_LEDGER_REF_TYPE),
                                        JE.referenceId.eq(S.referenceId)
                                ).notExists()
                )
                .orderBy(S.id.asc())
                .fetch()
                .stream().map(PgSettlementResult::from).toList();
    }
}
