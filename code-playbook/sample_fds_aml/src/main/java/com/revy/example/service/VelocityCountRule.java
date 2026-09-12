package com.revy.example.service;

@Component
@Slf4j
public class VelocityCountRule implements FdsRule {

    @Value("${fds.rules.velocity.max-count-per-hour:10}")
    private int maxCountPerHour;

    private final TransactionRepository transactionRepository;

    public VelocityCountRule(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public FdsRuleCode getRuleCode() {
        return FdsRuleCode.VELOCITY_COUNT;
    }

    @Override
    public int getPriority() {
        return 10;
    }

    @Override
    public RuleResult evaluate(RuleContext context) {
        Transaction txn = context.currentTransaction();
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);

        long count = transactionRepository.countByAccountIdWithinHour(
            txn.getAccountId(), oneHourAgo
        );

        if (count >= maxCountPerHour) {
            int score = Math.min(100, 50 + (int)((count - maxCountPerHour) * 5));
            String evidence = """
                {"rule": "VELOCITY_COUNT", "count": %d, "threshold": %d, "period": "1H"}
                """.formatted(count, maxCountPerHour);

            log.warn("[FDS] VELOCITY_COUNT triggered. accountId={}, count={}", txn.getAccountId(), count);
            return RuleResult.triggered(getRuleCode(), score, evidence);
        }

        return RuleResult.notTriggered(getRuleCode());
    }
}