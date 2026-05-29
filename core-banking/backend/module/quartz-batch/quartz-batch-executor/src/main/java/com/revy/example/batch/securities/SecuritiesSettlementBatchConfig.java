package com.revy.example.batch.securities;

import com.revy.example.domain.account.AccountTx;
import com.revy.example.domain.account.enums.TxStatus;
import com.revy.example.domain.account.enums.TxType;
import com.revy.example.domain.billing.Settlement;
import com.revy.example.domain.billing.enums.SettlementStatus;
import com.revy.example.domain.billing.enums.SettlementType;
import com.revy.example.ledger.command.LedgerCommand;
import com.revy.example.ledger.command.dto.JournalLineInput;
import com.revy.example.ledger.command.dto.PostJournalEntryCommand;
import com.revy.example.ledger.reader.LedgerReader;
import com.revy.example.settlement.command.SettlementCommand;
import com.revy.example.settlement.command.dto.CreateSettlementCommand;
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
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 증권 정산 및 원장 전기 Spring Batch 설정 (2-Step).
 *
 * <h3>Step 1 — 정산 생성 (tradeSettlementStep)</h3>
 * <pre>
 * [Reader]   AccountTx (BUY/SELL, COMPLETED, 대상일, Settlement 미생성)
 *     ↓
 * [Processor] CreateSettlementCommand 생성
 *     ↓
 * [Writer]   SettlementCommand.create() + settle()
 * </pre>
 *
 * <h3>Step 2 — 원장 전기 (tradeLedgerPostingStep)</h3>
 * <pre>
 * [Reader]   Settlement (TRADE, SETTLED, 대상일, JournalEntry 미생성)
 *     ↓
 * [Processor] PostJournalEntryCommand 생성
 *             BUY:  DR 증권투자자산(1002) + DR 매매비용(5001) / CR 현금(1001)
 *             SELL: DR 현금(1001)         + DR 매매비용(5001) / CR 증권투자자산(1002)
 *     ↓
 * [Writer]   LedgerCommand.createAndPostJournal()
 * </pre>
 *
 * <h3>멱등성</h3>
 * Step 1: NOT EXISTS (Settlement where referenceId = tx.referenceId)<br>
 * Step 2: NOT EXISTS (JournalEntry where referenceType='TRADE_SETTLEMENT' and referenceId = s.referenceId)
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class SecuritiesSettlementBatchConfig {

    private static final int SETTLEMENT_CHUNK_SIZE    = 100;
    private static final int LEDGER_CHUNK_SIZE        = 50;

    static final String CASH_ACCOUNT_CODE         = "1001";
    static final String INVEST_ACCOUNT_CODE       = "1002";
    static final String TRADING_COST_ACCOUNT_CODE = "5001";

    private static final String LEDGER_REF_TYPE = "TRADE_SETTLEMENT";
    private static final String BUY_TAG         = "[BUY]";
    private static final String SELL_TAG        = "[SELL]";

    private final SettlementCommand settlementCommand;
    private final LedgerCommand     ledgerCommand;
    private final LedgerReader      ledgerReader;

    // ── Job ────────────────────────────────────────────────────────────────

    @Bean("securitiesSettlementJob")
    public Job securitiesSettlementJob(
            JobRepository jobRepository,
            @Qualifier("tradeSettlementStep")    Step tradeSettlementStep,
            @Qualifier("tradeLedgerPostingStep") Step tradeLedgerPostingStep) {
        return new JobBuilder("securitiesSettlementJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(tradeSettlementStep)
                .next(tradeLedgerPostingStep)
                .build();
    }

    // ══════════════════════════════════════════════════════════════════════
    // STEP 1 — 정산 생성
    // ══════════════════════════════════════════════════════════════════════

    @Bean
    public Step tradeSettlementStep(
            JobRepository              jobRepository,
            PlatformTransactionManager transactionManager,
            @Qualifier("completedTradeReader")    JpaCursorItemReader<AccountTx> completedTradeReader,
            @Qualifier("tradeSettlementProcessor") ItemProcessor<AccountTx, CreateSettlementCommand> tradeSettlementProcessor,
            @Qualifier("tradeSettlementWriter")    ItemWriter<CreateSettlementCommand> tradeSettlementWriter) {

        return new StepBuilder("tradeSettlementStep", jobRepository)
                .<AccountTx, CreateSettlementCommand>chunk(SETTLEMENT_CHUNK_SIZE)
                        .transactionManager(transactionManager)
                .reader(completedTradeReader)
                .processor(tradeSettlementProcessor)
                .writer(tradeSettlementWriter)
                .faultTolerant()
                .skip(IllegalStateException.class)
                .skip(IllegalArgumentException.class)
                .skipLimit(Integer.MAX_VALUE)
                .build();
    }

    /**
     * 체결 완료(COMPLETED) BUY/SELL 중 Settlement 미생성 건을 스트리밍으로 읽는다.
     *
     * <p>{@code @StepScope}: jobParameters['targetDate'] 가 스텝 실행 시점에 바인딩된다.
     */
    @Bean
    @StepScope
    public JpaCursorItemReader<AccountTx> completedTradeReader(
            EntityManagerFactory emf,
            @Value("#{jobParameters['targetDate']}") LocalDate targetDate) {

        LocalDate resolved = (targetDate != null) ? targetDate : LocalDate.now().minusDays(1);
        Instant from = resolved.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant to   = resolved.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        String jpql = """
                SELECT tx FROM AccountTx tx
                WHERE tx.txType IN :txTypes
                  AND tx.status  = :status
                  AND tx.tradedAt >= :from
                  AND tx.tradedAt  < :to
                  AND NOT EXISTS (
                      SELECT 1 FROM Settlement s
                      WHERE s.referenceId = tx.referenceId
                  )
                ORDER BY tx.id ASC
                """;

        return new JpaCursorItemReaderBuilder<AccountTx>()
                .name("completedTradeReader")
                .entityManagerFactory(emf)
                .queryString(jpql)
                .parameterValues(Map.of(
                        "txTypes", List.of(TxType.BUY, TxType.SELL),
                        "status",  TxStatus.COMPLETED,
                        "from",    from,
                        "to",      to
                ))
                .build();
    }

    /**
     * AccountTx → CreateSettlementCommand 변환.
     *
     * <p>Settlement note 에 BUY/SELL 태그를 저장해 Step 2 에서 분개 방향을 식별한다.
     *
     * <ul>
     *   <li>BUY: grossAmount = price×qty, netAmount = gross + fee + tax (총 현금 유출)</li>
     *   <li>SELL: grossAmount = price×qty, netAmount = gross − fee − tax (순 현금 유입)</li>
     * </ul>
     */
    @Bean
    public ItemProcessor<AccountTx, CreateSettlementCommand> tradeSettlementProcessor() {
        return tx -> {
            boolean    isBuy  = tx.getTxType() == TxType.BUY;
            String     tag    = isBuy ? BUY_TAG : SELL_TAG;
            BigDecimal gross  = tx.getPrice().multiply(tx.getQuantity());
            BigDecimal fee    = tx.getFee();
            BigDecimal tax    = tx.getTax();
            BigDecimal net    = isBuy
                    ? gross.add(fee).add(tax)
                    : gross.subtract(fee).subtract(tax);

            LocalDate settlementDate = LocalDate.ofInstant(tx.getTradedAt(), ZoneOffset.UTC);
            String note = "%s 증권 체결 정산 accountId=%d stockId=%d"
                    .formatted(tag, tx.getAccountId(), tx.getStockId());

            log.debug("[TradeSettlementProcessor] {} referenceId={} gross={} net={}",
                    tag, tx.getReferenceId(), gross, net);

            return new CreateSettlementCommand(
                    tx.getAccountId(), SettlementType.TRADE, settlementDate,
                    "KRW", gross, fee, tax, net, tx.getReferenceId(), note
            );
        };
    }

    /** Settlement 생성 후 즉시 SETTLED 처리. */
    @Bean
    public ItemWriter<CreateSettlementCommand> tradeSettlementWriter() {
        return chunk -> {
            for (CreateSettlementCommand cmd : chunk.getItems()) {
                try {
                    Long settlementId = settlementCommand.create(cmd);
                    settlementCommand.settle(settlementId);
                    log.info("[TradeSettlementWriter] 정산 완료 referenceId={} id={}",
                            cmd.referenceId(), settlementId);
                } catch (Exception e) {
                    log.error("[TradeSettlementWriter] 정산 실패 referenceId={} error={}",
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
    public Step tradeLedgerPostingStep(
            JobRepository              jobRepository,
            PlatformTransactionManager transactionManager,
            @Qualifier("settledTradeSettlementReader") JpaCursorItemReader<Settlement> settledTradeSettlementReader,
            @Qualifier("tradeLedgerProcessor")         ItemProcessor<Settlement, PostJournalEntryCommand> tradeLedgerProcessor,
            @Qualifier("tradeLedgerWriter")            ItemWriter<PostJournalEntryCommand> tradeLedgerWriter) {

        return new StepBuilder("tradeLedgerPostingStep", jobRepository)
                .<Settlement, PostJournalEntryCommand>chunk(LEDGER_CHUNK_SIZE)
                        .transactionManager(transactionManager)
                .reader(settledTradeSettlementReader)
                .processor(tradeLedgerProcessor)
                .writer(tradeLedgerWriter)
                .faultTolerant()
                .skip(IllegalStateException.class)
                .skip(IllegalArgumentException.class)
                .skipLimit(Integer.MAX_VALUE)
                .build();
    }

    /** SETTLED 상태의 TRADE 정산 중 원장 미전기 건을 스트리밍으로 읽는다. */
    @Bean
    @StepScope
    public JpaCursorItemReader<Settlement> settledTradeSettlementReader(
            EntityManagerFactory emf,
            @Value("#{jobParameters['targetDate']}") LocalDate targetDate) {

        LocalDate resolved = (targetDate != null) ? targetDate : LocalDate.now().minusDays(1);

        String jpql = """
                SELECT s FROM Settlement s
                WHERE s.type   = :type
                  AND s.status = :status
                  AND s.settlementDate = :targetDate
                  AND NOT EXISTS (
                      SELECT 1 FROM JournalEntry je
                      WHERE je.referenceType = :refType
                        AND je.referenceId   = s.referenceId
                  )
                ORDER BY s.id ASC
                """;

        return new JpaCursorItemReaderBuilder<Settlement>()
                .name("settledTradeSettlementReader")
                .entityManagerFactory(emf)
                .queryString(jpql)
                .parameterValues(Map.of(
                        "type",       SettlementType.TRADE,
                        "status",     SettlementStatus.SETTLED,
                        "targetDate", resolved,
                        "refType",    LEDGER_REF_TYPE
                ))
                .build();
    }

    /**
     * Settlement → PostJournalEntryCommand 변환.
     *
     * <p>Settlement.note 의 BUY/SELL 태그로 분개 방향을 결정.
     *
     * <h4>BUY</h4>
     * DR 증권투자자산(1002) = grossAmount<br>
     * DR 매매비용(5001)      = fee + tax<br>
     * CR 현금(1001)          = netAmount
     *
     * <h4>SELL</h4>
     * DR 현금(1001)          = netAmount<br>
     * DR 매매비용(5001)      = fee + tax<br>
     * CR 증권투자자산(1002)  = grossAmount
     */
    @Bean
    public ItemProcessor<Settlement, PostJournalEntryCommand> tradeLedgerProcessor() {
        AtomicReference<Long> cashIdRef   = new AtomicReference<>();
        AtomicReference<Long> investIdRef = new AtomicReference<>();
        AtomicReference<Long> costIdRef   = new AtomicReference<>();

        return settlement -> {
            long cashId   = resolveAccountId(cashIdRef,   CASH_ACCOUNT_CODE);
            long investId = resolveAccountId(investIdRef, INVEST_ACCOUNT_CODE);
            long costId   = resolveAccountId(costIdRef,   TRADING_COST_ACCOUNT_CODE);

            String     note      = settlement.getNote() != null ? settlement.getNote() : "";
            boolean    isBuy     = note.contains(BUY_TAG);
            BigDecimal gross     = settlement.getGrossAmount();
            BigDecimal tradeCost = settlement.getFeeAmount().add(settlement.getTaxAmount());
            BigDecimal net       = settlement.getNetAmount();
            String     currency  = settlement.getCurrency();

            List<JournalLineInput> lines = isBuy
                    ? buildBuyLines(investId, costId, cashId, gross, tradeCost, net, currency, settlement)
                    : buildSellLines(cashId, costId, investId, gross, tradeCost, net, currency, settlement);

            String journalNumber = "JRNL-TRADE-" + settlement.getReferenceId();
            String side          = isBuy ? "매수" : "매도";

            log.debug("[TradeLedgerProcessor] {} referenceId={} gross={} net={}",
                    side, settlement.getReferenceId(), gross, net);

            return new PostJournalEntryCommand(
                    journalNumber,
                    settlement.getSettlementDate(),
                    "증권 %s 원장 전기 settlementId=%d".formatted(side, settlement.getId()),
                    LEDGER_REF_TYPE,
                    settlement.getReferenceId(),
                    lines
            );
        };
    }

    @Bean
    public ItemWriter<PostJournalEntryCommand> tradeLedgerWriter() {
        return chunk -> {
            for (PostJournalEntryCommand cmd : chunk.getItems()) {
                try {
                    Long journalId = ledgerCommand.createAndPostJournal(cmd);
                    log.info("[TradeLedgerWriter] 전기 완료 referenceId={} journalId={}",
                            cmd.referenceId(), journalId);
                } catch (Exception e) {
                    log.error("[TradeLedgerWriter] 전기 실패 referenceId={} error={}",
                            cmd.referenceId(), e.getMessage(), e);
                    throw e;
                }
            }
        };
    }

    // ── 내부 헬퍼 ─────────────────────────────────────────────────────────

    /** DR 증권투자자산 = gross, DR 매매비용 = tradeCost, CR 현금 = net */
    private List<JournalLineInput> buildBuyLines(
            long investId, long costId, long cashId,
            BigDecimal gross, BigDecimal tradeCost, BigDecimal net,
            String currency, Settlement s) {
        return List.of(
                JournalLineInput.debit(investId, gross,     currency,
                        "매수 원가 settlementId=" + s.getId()),
                JournalLineInput.debit(costId,   tradeCost, currency,
                        "매수 수수료·세금"),
                JournalLineInput.credit(cashId,  net,       currency,
                        "매수 대금 지급")
        );
    }

    /** DR 현금 = net, DR 매매비용 = tradeCost, CR 증권투자자산 = gross */
    private List<JournalLineInput> buildSellLines(
            long cashId, long costId, long investId,
            BigDecimal gross, BigDecimal tradeCost, BigDecimal net,
            String currency, Settlement s) {
        return List.of(
                JournalLineInput.debit(cashId,    net,       currency,
                        "매도 대금 수령"),
                JournalLineInput.debit(costId,    tradeCost, currency,
                        "매도 수수료·세금"),
                JournalLineInput.credit(investId, gross,     currency,
                        "매도 원가 제거 settlementId=" + s.getId())
        );
    }

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
