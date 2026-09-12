package com.revy.example.service;

@Component
@Slf4j
@RequiredArgsConstructor
public class FdsAlertEventHandler {

    private final NotificationService notificationService;
    private final AuditLogRepository auditLogRepository;

    @EventListener
    @Async  // Virtual Thread 환경에서는 별도 스레드풀 없이 처리 가능
    public void handleFdsAlertCreated(FdsAlertCreatedEvent event) {
        log.info("[EVENT] FdsAlert created. alertId={}, severity={}", event.alertId(), event.severity());

        // CRITICAL이면 즉시 알림
        if ("CRITICAL".equals(event.severity())) {
            notificationService.sendUrgentAlert(
                "긴급 FDS 알림: %s 룰 위반 - 계좌 %s".formatted(event.ruleCode(), event.accountId())
            );
        }

        // 감사 로그 기록
        auditLogRepository.save(AuditLog.of(
            "FDS_ALERT_CREATED",
            event.alertId().toString(),
            Map.of("ruleCode", event.ruleCode(), "severity", event.severity(), "score", event.score())
        ));
    }
}