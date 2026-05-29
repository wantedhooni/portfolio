package com.revy.example.batch.insurance;

import com.revy.example.domain.insurance.PremiumPayment;
import com.revy.example.domain.insurance.enums.PaymentStatus;
import com.revy.example.ledger.command.LedgerCommand;
import com.revy.example.ledger.command.dto.JournalLineInput;
import com.revy.example.ledger.command.dto.PostJournalEntryCommand;
import com.revy.example.ledger.reader.LedgerReader;
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
 * 보험료 원장 전기 Spring Batch 설정.
 *
 * <h3>처리 흐름</h3>
 * <pre>
 * [Reader]   PremiumPayment (PAID, dueDate = targetDate, 미전기)
 *     ↓
 * [Processor] PostJournalEntryCommand 생성
 *             DR 현금및현금성자산 (1001) — premium amount
 *             CR 보험료수입       (4001) — premium amount
 *     ↓
 * [Writer]   LedgerCommand.createAndPostJournal()
 * </pre>
 *
 * <h3>멱등성 보장</h3>
 * Reader JPQL: NOT EXISTS (JournalEntry where referenceType='PREMIUM_PAYMENT'
 *              and referenceId = pp.referenceId) → 이미 전기된 건 자동 스킵.
 *
 * <h3>스킵 정책</h3>
 * {@link IllegalStateException}, {@link IllegalArgumentException} 발생 시 해당 건 스킵 후 계속.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class InsuranceLedgerPostingBatchConfig {

    private static final int    CHUNK_SIZE       = 50;
    static final         String CASH_ACCOUNT_CODE    = "1001";
    static final         String REVENUE_ACCOUNT_CODE = "4001";
    private static final String REFERENCE_TYPE   = "PREMIUM_PAYMENT";

    private final LedgerCommand ledgerCommand;
    private final LedgerReader  ledgerReader;

    // ── Job ────────────────────────────────────────────────────────────────

    @Bean("insuranceLedgerPostingBatchJob")
    public Job insuranceLedgerPostingJob(
            JobRepository jobRepository,
            @Qualifier("premiumLedgerPostingStep")
            Step premiumLedgerPostingStep) {
        return new JobBuilder("insuranceLedgerPostingJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(premiumLedgerPostingStep)
                .build();
    }

    // ── Step ───────────────────────────────────────────────────────────────

    @Bean
    public Step premiumLedgerPostingStep(
            JobRepository              jobRepository,
            PlatformTransactionManager transactionManager,
            @Qualifier("premiumPaymentReader")
            JpaCursorItemReader<PremiumPayment> premiumPaymentReader,
            @Qualifier("premiumJournalProcessor")
            ItemProcessor<PremiumPayment, PostJournalEntryCommand> premiumJournalProcessor,
            @Qualifier("premiumJournalWriter")
            ItemWriter<PostJournalEntryCommand> premiumJournalWriter) {

        return new StepBuilder("premiumLedgerPostingStep", jobRepository)
                .<PremiumPayment, PostJournalEntryCommand>chunk(CHUNK_SIZE)
                        .transactionManager(transactionManager)
                .reader(premiumPaymentReader)
                .processor(premiumJournalProcessor)
                .writer(premiumJournalWriter)
                .faultTolerant()
                .skip(IllegalStateException.class)
                .skip(IllegalArgumentException.class)
                .skipLimit(Integer.MAX_VALUE)
                .build();
    }

    // ── Reader ─────────────────────────────────────────────────────────────

    /**
     * PAID 상태이고 해당 due_date를 가지며, 아직 원장 전기되지 않은 PremiumPayment를 스트리밍으로 읽는다.
     *
     * <p>{@code @StepScope}: jobParameters['targetDate'] 가 스텝 실행 시점에 바인딩된다.
     */
    @Bean
    @StepScope
    public JpaCursorItemReader<PremiumPayment> premiumPaymentReader(
            EntityManagerFactory emf,
            @Value("#{jobParameters['targetDate']}") LocalDate targetDate) {

        LocalDate resolved = (targetDate != null) ? targetDate : LocalDate.now().minusDays(1);

        String jpql = """
                SELECT pp FROM PremiumPayment pp
                WHERE pp.status = :status
                  AND pp.dueDate = :targetDate
                  AND NOT EXISTS (
                      SELECT 1 FROM JournalEntry je
                      WHERE je.referenceType = :refType
                        AND je.referenceId   = pp.referenceId
                  )
                ORDER BY pp.id ASC
                """;

        return new JpaCursorItemReaderBuilder<PremiumPayment>()
                .name("premiumPaymentReader")
                .entityManagerFactory(emf)
                .queryString(jpql)
                .parameterValues(Map.of(
                        "status",     PaymentStatus.PAID,
                        "targetDate", resolved,
                        "refType",    REFERENCE_TYPE
                ))
                .build();
    }

    // ── Processor ──────────────────────────────────────────────────────────

    /**
     * PremiumPayment → PostJournalEntryCommand 변환.
     *
     * <p>계정 코드(1001, 4001) 조회는 최초 1회만 수행하고 AtomicReference 에 캐시한다.
     */
    @Bean
    public ItemProcessor<PremiumPayment, PostJournalEntryCommand> premiumJournalProcessor() {
        AtomicReference<Long> cashIdRef    = new AtomicReference<>();
        AtomicReference<Long> revenueIdRef = new AtomicReference<>();

        return payment -> {
            long cashAccountId    = resolveAccountId(cashIdRef,    CASH_ACCOUNT_CODE);
            long revenueAccountId = resolveAccountId(revenueIdRef, REVENUE_ACCOUNT_CODE);

            BigDecimal amount = payment.getAmount();

            List<JournalLineInput> lines = List.of(
                    JournalLineInput.debit(
                            cashAccountId, amount, payment.getCurrency(),
                            "보험료 수령 policyId=" + payment.getPolicyId()),
                    JournalLineInput.credit(
                            revenueAccountId, amount, payment.getCurrency(),
                            "보험료수입 인식 policyId=" + payment.getPolicyId())
            );

            String journalNumber = "JRNL-PREM-" + payment.getReferenceId();

            log.debug("[InsuranceLedgerProcessor] policyId={} amount={} journalNumber={}",
                    payment.getPolicyId(), amount, journalNumber);

            return new PostJournalEntryCommand(
                    journalNumber,
                    payment.getDueDate(),
                    "보험료 원장 전기 policyId=" + payment.getPolicyId(),
                    REFERENCE_TYPE,
                    payment.getReferenceId(),
                    lines
            );
        };
    }

    // ── Writer ─────────────────────────────────────────────────────────────

    @Bean
    public ItemWriter<PostJournalEntryCommand> premiumJournalWriter() {
        return chunk -> {
            for (PostJournalEntryCommand cmd : chunk.getItems()) {
                try {
                    Long journalId = ledgerCommand.createAndPostJournal(cmd);
                    log.info("[InsuranceLedgerWriter] 전기 완료 referenceId={} journalId={}",
                            cmd.referenceId(), journalId);
                } catch (Exception e) {
                    log.error("[InsuranceLedgerWriter] 전기 실패 referenceId={} error={}",
                            cmd.referenceId(), e.getMessage(), e);
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
                .orElseThrow(() -> new IllegalStateException(
                        "원장 계정을 찾을 수 없습니다. code=" + code
                                + " (V20260529000001 마이그레이션 실행 여부 확인)"));
        ref.compareAndSet(null, id);
        return id;
    }
}
