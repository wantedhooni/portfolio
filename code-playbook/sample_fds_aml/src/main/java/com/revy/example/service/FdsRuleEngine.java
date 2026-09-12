package com.revy.example.service;

@Service
@Slf4j
@RequiredArgsConstructor
public class FdsRuleEngine {

    private final List<FdsRule> rules;  // Spring이 구현체 자동 주입
    private final FdsAlertRepository alertRepository;
    private final TransactionRepository transactionRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public List<FdsAlert> evaluate(Transaction transaction, Account account, Customer customer) {
        // 최근 1시간 거래 이력 조회
        List<Transaction> recentTxns = transactionRepository
            .findByAccountIdWithinPeriod(
                transaction.getAccountId(),
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now()
            );

        RuleContext context = new RuleContext(
            transaction, account, customer,
            recentTxns,
            loadBlacklistIps(),
            loadBlacklistAccounts()
        );

        // 우선순위 순 정렬 후 평가
        List<FdsAlert> alerts = rules.stream()
            .sorted(Comparator.comparingInt(FdsRule::getPriority))
            .map(rule -> {
                try {
                    return rule.evaluate(context);
                } catch (Exception e) {
                    log.error("[FDS] Rule evaluation failed. rule={}, txnId={}",
                        rule.getRuleCode(), transaction.getTransactionId(), e);
                    return RuleResult.notTriggered(rule.getRuleCode());
                }
            })
            .filter(RuleResult::triggered)
            .map(result -> FdsAlert.of(
                transaction.getTransactionId(),
                transaction.getAccountId(),
                result.ruleCode(),
                result.severity(),
                result.score(),
                result.evidence()
            ))
            .toList();

        if (!alerts.isEmpty()) {
            alertRepository.saveAll(alerts);
            log.info("[FDS] {} alerts generated for txnId={}", alerts.size(), transaction.getTransactionId());
        }

        return alerts;
    }

    // 실제 구현에서는 Redis 또는 DB에서 캐싱하여 로드
    private Set<String> loadBlacklistIps() {
        return Set.of(); // TODO: 블랙리스트 관리 모듈 연동
    }

    private Set<UUID> loadBlacklistAccounts() {
        return Set.of(); // TODO: 블랙리스트 관리 모듈 연동
    }
}