package com.revy.example.service;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class CtrDetectionService {

    @Value("${aml.ctr.threshold:10000000}")
    private BigDecimal ctrThreshold; // 기본 1천만원

    private final TransactionRepository transactionRepository;
    private final AmlCaseRepository amlCaseRepository;
    private final CustomerRepository customerRepository;

    /**
     * CTR 대상 탐지:
     * 1) 단일 현금 거래가 CTR 기준 이상
     * 2) 당일 현금 거래 합산이 CTR 기준 이상 (분할거래 포함)
     */
    public Optional<AmlCase> detectCtr(Transaction transaction) {
        // 현금거래(입금/출금)만 대상
        if (!isCashTransaction(transaction)) {
            return Optional.empty();
        }

        BigDecimal amount = transaction.getAmount().getAmount();

        // 단일 건 초과
        if (amount.compareTo(ctrThreshold) >= 0) {
            return Optional.of(createCtrCase(transaction, List.of(transaction), amount));
        }

        // 당일 누적 합산 체크
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        BigDecimal dailyTotal = transactionRepository.sumAmountByAccountIdAndType(
            transaction.getAccountId(),
            transaction.getTransactionType(),
            startOfDay,
            LocalDateTime.now()
        );

        if (dailyTotal != null && dailyTotal.compareTo(ctrThreshold) >= 0) {
            List<Transaction> todayTxns = transactionRepository.findByAccountIdWithinPeriod(
                transaction.getAccountId(), startOfDay, LocalDateTime.now()
            );
            return Optional.of(createCtrCase(transaction, todayTxns, dailyTotal));
        }

        return Optional.empty();
    }

    private AmlCase createCtrCase(
        Transaction trigger, List<Transaction> related, BigDecimal totalAmount
    ) {
        // 중복 케이스 방지: 동일 계좌 당일 CTR 케이스가 이미 있으면 스킵
        boolean exists = amlCaseRepository.existsByCustomerIdAndCaseTypeAndDetectionDate(
            trigger.getAccountId(), AmlCaseType.CTR, LocalDate.now()
        );
        if (exists) {
            log.info("[AML-CTR] Already exists for accountId={}", trigger.getAccountId());
            return amlCaseRepository.findByCustomerIdAndCaseTypeAndDetectionDate(
                trigger.getAccountId(), AmlCaseType.CTR, LocalDate.now()
            ).orElseThrow();
        }

        List<UUID> relatedIds = related.stream()
            .map(Transaction::getTransactionId).toList();

        AmlCase amlCase = AmlCase.createCtr(trigger.getAccountId(), totalAmount, relatedIds);
        AmlCase saved = amlCaseRepository.save(amlCase);

        log.info("[AML-CTR] Case created. caseId={}, accountId={}, amount={}",
            saved.getCaseId(), trigger.getAccountId(), totalAmount);

        return saved;
    }

    private boolean isCashTransaction(Transaction txn) {
        return txn.getTransactionType() == TransactionType.DEPOSIT
            || txn.getTransactionType() == TransactionType.WITHDRAWAL;
    }
}