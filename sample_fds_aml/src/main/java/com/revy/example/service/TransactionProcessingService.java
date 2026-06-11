package com.revy.example.service;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionProcessingService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final FdsRuleEngine fdsRuleEngine;
    private final CtrDetectionService ctrDetectionService;
    private final StrDetectionService strDetectionService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 거래 처리 메인 파이프라인
     * Java 21 Virtual Thread 환경에서 동작
     */
    @Transactional
    public TransactionResult process(CreateTransactionCommand command) {
        // 1. 거래 생성
        Account account = accountRepository.findById(command.accountId())
            .orElseThrow(() -> new EntityNotFoundException("Account not found: " + command.accountId()));

        Customer customer = customerRepository.findById(account.getCustomerId())
            .orElseThrow(() -> new EntityNotFoundException("Customer not found"));

        Transaction transaction = Transaction.create(
            command.accountId(),
            command.transactionType(),
            MoneyEmbeddable.of(command.amount(), "KRW"),
            command.channel()
        );
        transaction = transactionRepository.save(transaction);

        // 2. FDS 룰 평가
        List<FdsAlert> alerts = fdsRuleEngine.evaluate(transaction, account, customer);

        // 3. 거래 승인/차단 결정
        boolean hasBlockingAlert = alerts.stream()
            .anyMatch(a -> a.getSeverity() == AlertSeverity.CRITICAL);

        if (hasBlockingAlert) {
            transaction.block("CRITICAL FDS Alert triggered");
            transactionRepository.save(transaction);
            log.warn("[TXN] Blocked. txnId={}", transaction.getTransactionId());
        } else {
            transaction.approve();
            transactionRepository.save(transaction);
        }

        // 4. AML 탐지 (승인된 거래만)
        List<AmlCase> amlCases = new ArrayList<>();
        if (transaction.getStatus() == TransactionStatus.APPROVED) {
            ctrDetectionService.detectCtr(transaction).ifPresent(amlCases::add);
            amlCases.addAll(strDetectionService.detectStr(transaction, alerts));
        }

        // 5. 이벤트 발행 (트랜잭션 커밋 후)
        publishEvents(transaction, alerts, amlCases);

        return new TransactionResult(
            transaction.getTransactionId(),
            transaction.getStatus(),
            alerts,
            amlCases
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    private void publishEvents(Transaction txn, List<FdsAlert> alerts, List<AmlCase> cases) {
        eventPublisher.publishEvent(new TransactionCreatedEvent(
            txn.getTransactionId(), txn.getAccountId(), null,
            txn.getAmount().getAmount(), txn.getTransactionType().name(),
            LocalDateTime.now()
        ));

        alerts.forEach(alert -> eventPublisher.publishEvent(new FdsAlertCreatedEvent(
            alert.getAlertId(), alert.getTransactionId(), alert.getAccountId(),
            alert.getRuleCode().name(), alert.getSeverity().name(), alert.getScore()
        )));

        cases.forEach(c -> eventPublisher.publishEvent(new AmlCaseCreatedEvent(
            c.getCaseId(), c.getCustomerId(), c.getCaseType().name(),
            c.getTotalAmount(), c.getReportDeadline()
        )));
    }
}