package com.revy.example.service;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class StrDetectionService {

    @Value("${aml.str.review-period-days:30}")
    private int reviewPeriodDays;

    private final TransactionRepository transactionRepository;
    private final AmlCaseRepository amlCaseRepository;
    private final FdsAlertRepository alertRepository;

    /**
     * STR 대상 판단:
     * - FDS High/Critical 알림이 발생한 거래
     * - 분할거래 패턴 (Structuring) 탐지
     * - 휴면 계좌 갑작스런 대규모 거래
     */
    public List<AmlCase> detectStr(Transaction transaction, List<FdsAlert> fdsAlerts) {
        List<AmlCase> cases = new ArrayList<>();

        // 1. FDS High/Critical → STR 자동 생성
        boolean hasCriticalAlert = fdsAlerts.stream()
            .anyMatch(a -> a.getSeverity() == AlertSeverity.CRITICAL
                        || a.getSeverity() == AlertSeverity.HIGH);

        if (hasCriticalAlert) {
            List<UUID> relatedIds = fdsAlerts.stream()
                .map(FdsAlert::getTransactionId).distinct().toList();
            AmlCase strCase = AmlCase.createStr(
                transaction.getAccountId(),
                transaction.getAmount().getAmount(),
                relatedIds
            );
            strCase.setSummary(buildStrSummary(transaction, fdsAlerts));
            cases.add(amlCaseRepository.save(strCase));
        }

        // 2. 분할거래 패턴 탐지
        Optional<AmlCase> structuringCase = detectStructuring(transaction);
        structuringCase.ifPresent(cases::add);

        return cases;
    }

    private Optional<AmlCase> detectStructuring(Transaction transaction) {
        // 최근 30일간 동일 계좌 거래 조회
        LocalDateTime from = LocalDateTime.now().minusDays(reviewPeriodDays);
        List<Transaction> history = transactionRepository.findByAccountIdWithinPeriod(
            transaction.getAccountId(), from, LocalDateTime.now()
        );

        // CTR 기준(1천만원)의 90% 미만 거래가 5건 이상, 합산은 기준 초과
        BigDecimal ctrBase = new BigDecimal("10000000");
        BigDecimal ctrNinetyPct = ctrBase.multiply(BigDecimal.valueOf(0.9));

        List<Transaction> suspiciousTxns = history.stream()
            .filter(t -> t.getAmount().getAmount().compareTo(ctrNinetyPct) < 0)
            .filter(t -> t.getAmount().getAmount().compareTo(new BigDecimal("1000000")) >= 0) // 100만원 이상
            .toList();

        if (suspiciousTxns.size() >= 5) {
            BigDecimal total = suspiciousTxns.stream()
                .map(t -> t.getAmount().getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (total.compareTo(ctrBase) >= 0) {
                List<UUID> ids = suspiciousTxns.stream()
                    .map(Transaction::getTransactionId).toList();
                AmlCase strCase = AmlCase.createStr(transaction.getAccountId(), total, ids);
                strCase.setSummary("분할거래(Structuring) 의심: %d건 합계 %s원".formatted(
                    suspiciousTxns.size(), total.toPlainString()
                ));
                return Optional.of(amlCaseRepository.save(strCase));
            }
        }

        return Optional.empty();
    }

    private String buildStrSummary(Transaction txn, List<FdsAlert> alerts) {
        String alertSummary = alerts.stream()
            .map(a -> "[%s:%s]".formatted(a.getRuleCode(), a.getSeverity()))
            .collect(Collectors.joining(", "));
        return "FDS 고위험 알림 연계 STR. 알림: %s. 거래금액: %s원".formatted(
            alertSummary, txn.getAmount().getAmount().toPlainString()
        );
    }
}