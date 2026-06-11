package com.revy.example.infra;

@Component
@RequiredArgsConstructor
public class FdsMetricsCollector {

    private final MeterRegistry meterRegistry;
    private final Counter fdsAlertCounter;
    private final Counter amlCaseCounter;

    @PostConstruct
    public void init() {
        // Gauge 등록 (운영 대시보드용)
        Gauge.builder("fds.alert.open.count", this, FdsMetricsCollector::getOpenAlertCount)
            .description("현재 열린 FDS 알림 수")
            .register(meterRegistry);
    }

    public void recordAlert(AlertSeverity severity) {
        meterRegistry.counter("fds.alert.created",
            "severity", severity.name()
        ).increment();
    }

    public void recordAmlCase(AmlCaseType caseType) {
        meterRegistry.counter("aml.case.created",
            "type", caseType.name()
        ).increment();
    }

    public void recordRuleEvaluation(FdsRuleCode ruleCode, boolean triggered, long durationMs) {
        meterRegistry.timer("fds.rule.evaluation",
            "rule", ruleCode.name(),
            "triggered", String.valueOf(triggered)
        ).record(durationMs, TimeUnit.MILLISECONDS);
    }

    private double getOpenAlertCount() {
        // Repository에서 조회
        return 0; // TODO
    }
}