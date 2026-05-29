package com.revy.example.pg.reader;

import com.revy.example.pg.reader.dto.PgMerchantResult;
import com.revy.example.pg.reader.dto.PgPaymentResult;
import com.revy.example.pg.reader.dto.PgSettlementResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PgReader {

    // ── Merchant ─────────────────────────────────────────────────
    Optional<PgMerchantResult> findMerchantById(Long id);
    Optional<PgMerchantResult> findMerchantByCode(String merchantCode);
    boolean existsMerchantByCode(String merchantCode);
    /** 배치 Step 1 Reader — 활성 가맹점 전체 */
    List<PgMerchantResult> findAllActiveMerchants();

    // ── Payment ──────────────────────────────────────────────────
    Optional<PgPaymentResult> findPaymentById(Long id);
    boolean existsPaymentByOrderNo(String orderNo);
    Page<PgPaymentResult> searchPayments(Pageable pageable, Long merchantId);

    /**
     * 배치 Step 1 Processor — 가맹점별 정산 대상 결제 조회.
     *
     * <p>조건: status=APPROVED, approvedAt 날짜 = targetDate, pgSettlementId IS NULL
     */
    List<PgPaymentResult> findApprovedPaymentsForSettlement(Long merchantId, LocalDate targetDate);

    // ── Settlement ───────────────────────────────────────────────
    Optional<PgSettlementResult> findSettlementById(Long id);
    boolean existsSettlementByReferenceId(String referenceId);
    Page<PgSettlementResult> searchSettlements(Pageable pageable, Long merchantId);

    /**
     * 배치 Step 2 Reader — 원장 전기 대상 PG 정산 조회.
     *
     * <p>조건: status=SETTLED, settlementDate = targetDate, JournalEntry 미생성
     */
    List<PgSettlementResult> findSettledForLedger(LocalDate settlementDate);
}
