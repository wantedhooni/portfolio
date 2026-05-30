package com.revy.example.batch.pg;

import com.revy.example.domain.pg.PgMerchant;
import com.revy.example.domain.pg.PgSettlement;
import com.revy.example.ledger.command.LedgerCommand;
import com.revy.example.ledger.command.dto.JournalLineInput;
import com.revy.example.ledger.command.dto.PostJournalEntryCommand;
import com.revy.example.ledger.reader.LedgerReader;
import com.revy.example.pg.command.PgCommand;
import com.revy.example.pg.command.dto.CreatePgSettlementCommand;
import com.revy.example.pg.reader.PgReader;
import com.revy.example.pg.reader.dto.PgPaymentResult;
import com.revy.example.pg.reader.dto.PgSettlementResult;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.parameters.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.database.JpaCursorItemReader;
import org.springframework.batch.infrastructure.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * PG 정산 및 원장 전기 Spring Batch 설정 (2-Step).
 *
 * <h3>Step 1 — PG 정산 생성 (pgSettlementStep)</h3>
 * <pre>
 * [Reader]   PgMerchant (활성 가맹점 전체)
 *     ↓
 * [Processor] 가맹점별 APPROVED 결제 집계 → CreatePgSettlementCommand
 *             · 멱등성: referenceId 중복 시 null 반환(스킵)
 *             · 결제 없으면 null 반환(스킵)
 *     ↓
 * [Writer]   PgCommand.createSettlement() + completeSettlement() + 결제 연결
 * </pre>
 *
 * <h3>Step 2 — 원장 전기 (pgLedgerPostingStep)</h3>
 * <pre>
 * [Reader]   PgSettlement (SETTLED, settlementDate = targetDate, 미전기)
 *     ↓
 * [Processor] PostJournalEntryCommand 생성
 *             DR 현금          (1001) = totalAmount
 *             CR 가맹점정산부채 (2002) = netAmount
 *             CR 수수료수입    (4002) = commissionAmount
 *     ↓
 * [Writer]   LedgerCommand.createAndPostJournal()
 * </pre>
 *
 * <h3>멱등성</h3>
 * Step 1: referenceId = "PGSTL-{merchantId}-{targetDate}" NOT EXISTS 체크.<br>
 * Step 2: JournalEntry.referenceType='PG_SETTLEMENT' NOT EXISTS 체크 (Reader 쿼리).
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class PgSettlementBatchConfig {

    private static final int SETTLEMENT_CHUNK_SIZE = 20;   // 가맹점 단위 처리
    private static final int LEDGER_CHUNK_SIZE = 50;

    // 원장 계정 코드
    static final String CASH_ACCOUNT_CODE = "1001";
    static final String MERCHANT_LIAB_CODE = "2002"; // 가맹점정산부채
    static final String FEE_REVENUE_CODE = "4002"; // 수수료수입

    private static final String LEDGER_REF_TYPE = "PG_SETTLEMENT";

    private final PgCommand pgCommand;
    private final PgReader pgReader;
    private final LedgerCommand ledgerCommand;
    private final LedgerReader ledgerReader;

    // ── Job ────────────────────────────────────────────────────────────────

    @Bean("pgSettlementJob")
    public Job pgSettlementJob(JobRepository jobRepository, @Qualifier("pgSettlementStep") Step pgSettlementStep, @Qualifier("pgLedgerPostingStep") Step pgLedgerPostingStep) {
        return new JobBuilder("pgSettlementJob", jobRepository)
            .incrementer(new RunIdIncrementer())
            .start(pgSettlementStep)
            .next(pgLedgerPostingStep)
            .build();
    }

    // ══════════════════════════════════════════════════════════════════════
    // STEP 1 — PG 정산 생성
    // ══════════════════════════════════════════════════════════════════════

    @Bean
    public Step pgSettlementStep(JobRepository jobRepository, PlatformTransactionManager transactionManager, @Qualifier("pgMerchantReader") JpaCursorItemReader<PgMerchant> pgMerchantReader, @Qualifier("pgSettlementProcessor") ItemProcessor<PgMerchant, CreatePgSettlementCommand> pgSettlementProcessor, @Qualifier("pgSettlementWriter") ItemWriter<CreatePgSettlementCommand> pgSettlementWriter) {

        return new StepBuilder("pgSettlementStep", jobRepository)
            .<PgMerchant, CreatePgSettlementCommand>chunk(SETTLEMENT_CHUNK_SIZE)
            .transactionManager(transactionManager)
            .reader(pgMerchantReader)
            .processor(pgSettlementProcessor)
            .writer(pgSettlementWriter)
            .faultTolerant()
            .skip(IllegalStateException.class)
            .skip(IllegalArgumentException.class)
            .skipLimit(Integer.MAX_VALUE)
            .build();
    }

    /**
     * 활성 가맹점을 ID 오름차순으로 스트리밍한다.
     */
    @Bean
    public JpaCursorItemReader<PgMerchant> pgMerchantReader(EntityManagerFactory emf) {
        return new JpaCursorItemReaderBuilder<PgMerchant>().name("pgMerchantReader")
                                                           .entityManagerFactory(emf)
                                                           .queryString(
                                                               "SELECT m FROM PgMerchant m WHERE m.isActive = true ORDER BY m.id ASC")
                                                           .build();
    }

    /**
     * 가맹점별 정산 집계 Processor.
     *
     * <p>{@code @StepScope}: jobParameters['targetDate'] 를 스텝 실행 시점에 바인딩한다.
     * T+{settlementCycle} 기준으로 각 가맹점의 정산 대상 날짜(paymentDate)를 계산한다.
     *
     * <p>결제가 없거나 이미 정산된 가맹점은 {@code null} 반환 → Spring Batch 가 자동 필터링.
     */
    @Bean
    @StepScope
    public ItemProcessor<PgMerchant, CreatePgSettlementCommand> pgSettlementProcessor(@Value("#{jobParameters['targetDate']}") LocalDate targetDate) {

        LocalDate settlementDate = (targetDate != null) ? targetDate : LocalDate.now();

        return merchant -> {
            // 가맹점별 정산 기준일: settlementDate - settlementCycle
            LocalDate paymentDate = settlementDate.minusDays(merchant.getSettlementCycle());
            String referenceId = "PGSTL-" + merchant.getId() + "-" + paymentDate;

            // 멱등성 체크
            if (pgReader.existsSettlementByReferenceId(referenceId)) {
                log.debug("[PgSettlementProcessor] 이미 정산된 가맹점 스킵 merchantId={} date={}", merchant.getId(), paymentDate);
                return null;
            }

            // 정산 대상 결제 조회
            List<PgPaymentResult> payments = pgReader.findApprovedPaymentsForSettlement(merchant.getId(), paymentDate);

            if (payments.isEmpty()) {
                log.debug("[PgSettlementProcessor] 정산 대상 결제 없음 merchantId={} date={}", merchant.getId(), paymentDate);
                return null;
            }

            // 집계
            BigDecimal totalAmount = payments.stream()
                                             .map(PgPaymentResult::amount)
                                             .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal commissionAmount = payments.stream()
                                                  .map(PgPaymentResult::commissionAmount)
                                                  .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal netAmount = totalAmount.subtract(commissionAmount);
            List<Long> paymentIds = payments.stream()
                                            .map(PgPaymentResult::id)
                                            .toList();

            log.info("[PgSettlementProcessor] 정산 집계 merchantId={} paymentDate={} count={} total={}", merchant.getId(),
                     paymentDate, payments.size(), totalAmount);

            return new CreatePgSettlementCommand(merchant.getId(), paymentDate, settlementDate, merchant.getCurrency(),
                                                 payments.size(), totalAmount, commissionAmount, netAmount, referenceId,
                                                 paymentIds);
        };
    }

    /**
     * 정산 생성 + 즉시 완료 + 결제 연결 Writer.
     *
     * <p>청크 내에서 각 가맹점 정산을 독립적으로 처리한다.
     * 개별 실패는 로그 기록 후 예외를 전파해 Skip 정책이 적용되도록 한다.
     */
    @Bean
    public ItemWriter<CreatePgSettlementCommand> pgSettlementWriter() {
        return chunk -> {
            for (CreatePgSettlementCommand cmd : chunk.getItems()) {
                try {
                    Long settlementId = pgCommand.createSettlement(cmd);
                    pgCommand.completeSettlement(settlementId, cmd.paymentIds());
                    log.info("[PgSettlementWriter] 정산 완료 merchantId={} settlementId={} count={} net={}",
                             cmd.merchantId(), settlementId, cmd.paymentCount(), cmd.netAmount());
                } catch (Exception e) {
                    log.error("[PgSettlementWriter] 정산 실패 merchantId={} ref={} error={}", cmd.merchantId(),
                              cmd.referenceId(), e.getMessage(), e);
                    throw e;
                }
            }
        };
    }

    // ══════════════════════════════════════════════════════════════════════
    // STEP 2 — 원장 전기
    // ══════════════════════════════════════════════════════════════════════

    @Bean
    public Step pgLedgerPostingStep(JobRepository jobRepository, PlatformTransactionManager transactionManager, @Qualifier("pgSettledForLedgerReader") JpaCursorItemReader<PgSettlement> pgSettledForLedgerReader, @Qualifier("pgLedgerProcessor") ItemProcessor<PgSettlement, PostJournalEntryCommand> pgLedgerProcessor, @Qualifier("pgLedgerWriter") ItemWriter<PostJournalEntryCommand> pgLedgerWriter) {

        return new StepBuilder("pgLedgerPostingStep", jobRepository).<PgSettlement, PostJournalEntryCommand>chunk(
                                                                        LEDGER_CHUNK_SIZE)
                                                                    .transactionManager(transactionManager)
                                                                    .reader(pgSettledForLedgerReader)
                                                                    .processor(pgLedgerProcessor)
                                                                    .writer(pgLedgerWriter)
                                                                    .faultTolerant()
                                                                    .skip(IllegalStateException.class)
                                                                    .skip(IllegalArgumentException.class)
                                                                    .skipLimit(Integer.MAX_VALUE)
                                                                    .build();
    }

    /**
     * SETTLED 상태이고 원장 미전기인 PG 정산을 스트리밍으로 읽는다.
     *
     * <p>멱등성: NOT EXISTS (JournalEntry where referenceType='PG_SETTLEMENT' and referenceId=s.referenceId)
     */
    @Bean
    @StepScope
    public JpaCursorItemReader<PgSettlement> pgSettledForLedgerReader(EntityManagerFactory emf, @Value("#{jobParameters['targetDate']}") LocalDate targetDate) {

        LocalDate resolved = (targetDate != null) ? targetDate : LocalDate.now();

        String jpql = """
            SELECT s FROM PgSettlement s
            WHERE s.status = :status
              AND s.settlementDate = :settlementDate
              AND NOT EXISTS (
                  SELECT 1 FROM JournalEntry je
                  WHERE je.referenceType = :refType
                    AND je.referenceId   = s.referenceId
              )
            ORDER BY s.id ASC
            """;

        return new JpaCursorItemReaderBuilder<PgSettlement>().name("pgSettledForLedgerReader")
                                                             .entityManagerFactory(emf)
                                                             .queryString(jpql)
                                                             .parameterValues(Map.of("status",
                                                                                     com.revy.example.domain.pg.enums.PgSettlementStatus.SETTLED,
                                                                                     "settlementDate", resolved,
                                                                                     "refType", LEDGER_REF_TYPE))
                                                             .build();
    }

    /**
     * PgSettlement → PostJournalEntryCommand 변환.
     *
     * <h4>분개</h4>
     * <pre>
     * DR 현금          (1001) = totalAmount
     *    CR 가맹점정산부채 (2002) = netAmount
     *    CR 수수료수입    (4002) = commissionAmount
     * </pre>
     * 검증: totalAmount = netAmount + commissionAmount ✓
     */
    @Bean
    public ItemProcessor<PgSettlement, PostJournalEntryCommand> pgLedgerProcessor() {
        AtomicReference<Long> cashIdRef = new AtomicReference<>();
        AtomicReference<Long> liabIdRef = new AtomicReference<>();
        AtomicReference<Long> revenueIdRef = new AtomicReference<>();

        return settlement -> {
            long cashId = resolveAccountId(cashIdRef, CASH_ACCOUNT_CODE);
            long liabId = resolveAccountId(liabIdRef, MERCHANT_LIAB_CODE);
            long revenueId = resolveAccountId(revenueIdRef, FEE_REVENUE_CODE);

            BigDecimal total = settlement.getTotalAmount();
            BigDecimal net = settlement.getNetAmount();
            BigDecimal commission = settlement.getCommissionAmount();
            String currency = settlement.getCurrency();

            List<JournalLineInput> lines = List.of(
                JournalLineInput.debit(cashId, total, currency, "PG 결제 수령 merchantId=" + settlement.getMerchantId()),
                JournalLineInput.credit(liabId, net, currency, "가맹점 정산 지급 예정 merchantId=" + settlement.getMerchantId()),
                JournalLineInput.credit(revenueId, commission, currency,
                                        "PG 수수료 수입 merchantId=" + settlement.getMerchantId()));

            String journalNumber = "JRNL-PG-" + settlement.getReferenceId();

            log.debug("[PgLedgerProcessor] merchantId={} referenceId={} total={} commission={}",
                      settlement.getMerchantId(), settlement.getReferenceId(), total, commission);

            return new PostJournalEntryCommand(journalNumber, settlement.getSettlementDate(),
                                               "PG 정산 원장 전기 merchantId=" + settlement.getMerchantId(), LEDGER_REF_TYPE,
                                               settlement.getReferenceId(), lines);
        };
    }

    @Bean
    public ItemWriter<PostJournalEntryCommand> pgLedgerWriter() {
        return chunk -> {
            for (PostJournalEntryCommand cmd : chunk.getItems()) {
                try {
                    Long journalId = ledgerCommand.createAndPostJournal(cmd);
                    log.info("[PgLedgerWriter] 전기 완료 referenceId={} journalId={}", cmd.referenceId(), journalId);
                } catch (Exception e) {
                    log.error("[PgLedgerWriter] 전기 실패 referenceId={} error={}", cmd.referenceId(), e.getMessage(), e);
                    throw e;
                }
            }
        };
    }

    // ── 내부 ───────────────────────────────────────────────────────────────

    private long resolveAccountId(AtomicReference<Long> ref, String code) {
        Long cached = ref.get();
        if (cached != null) return cached;
        Long id = ledgerReader.findAccountByCode(code)
                              .map(r -> r.id())
                              .orElseThrow(() -> new IllegalStateException("원장 계정을 찾을 수 없습니다. code=" + code));
        ref.compareAndSet(null, id);
        return id;
    }
}
