package com.revy.example.domain;

import com.revy.example.domain.core.BaseEntity;
import com.revy.example.enums.AlertSeverity;
import com.revy.example.enums.AlertStatus;
import com.revy.example.enums.FdsRuleCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "fds_alerts",
    schema = "fds",
    indexes = {
        @Index(name = "idx_alert_txn_id",   columnList = "transaction_id"),
        @Index(name = "idx_alert_status",    columnList = "status"),
        @Index(name = "idx_alert_severity",  columnList = "severity"),
        @Index(name = "idx_alert_created",   columnList = "created_at")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FdsAlert extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "alert_id", columnDefinition = "uuid")
    private UUID alertId;

    @Column(name = "transaction_id", nullable = false)
    private UUID transactionId;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "rule_code", nullable = false, length = 50)
    private FdsRuleCode ruleCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 20)
    private AlertSeverity severity;  // LOW, MEDIUM, HIGH, CRITICAL

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AlertStatus status;  // OPEN, REVIEWING, CLOSED, FALSE_POSITIVE

    @Column(name = "score", nullable = false)
    private Integer score;  // 0~100

    // 탐지 근거 (JSON 저장)
    @Column(name = "evidence", columnDefinition = "jsonb")
    private String evidence;

    @Column(name = "analyst_note", length = 500)
    private String analystNote;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "resolved_by", length = 50)
    private String resolvedBy;

    public static FdsAlert of(
        UUID transactionId,
        UUID accountId,
        FdsRuleCode ruleCode,
        AlertSeverity severity,
        int score,
        String evidence
    ) {
        FdsAlert alert = new FdsAlert();
        alert.alertId = UUID.randomUUID();
        alert.transactionId = transactionId;
        alert.accountId = accountId;
        alert.ruleCode = ruleCode;
        alert.severity = severity;
        alert.status = AlertStatus.OPEN;
        alert.score = score;
        alert.evidence = evidence;
        return alert;
    }

    public void resolve(String analyst, boolean isFalsePositive, String note) {
        this.status = isFalsePositive ? AlertStatus.FALSE_POSITIVE : AlertStatus.CLOSED;
        this.resolvedBy = analyst;
        this.resolvedAt = LocalDateTime.now();
        this.analystNote = note;
    }
}
