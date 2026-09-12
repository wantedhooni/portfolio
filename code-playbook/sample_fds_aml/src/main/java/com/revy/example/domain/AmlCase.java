package com.revy.example.domain;

import com.revy.example.domain.core.BaseEntity;
import com.revy.example.enums.AmlCaseStatus;
import com.revy.example.enums.AmlCaseType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "aml_cases", schema = "fds")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AmlCase extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "case_id", columnDefinition = "uuid")
    private UUID caseId;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "case_type", nullable = false, length = 10)
    private AmlCaseType caseType;  // CTR, STR

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AmlCaseStatus status;

    @Column(name = "detection_date", nullable = false)
    private LocalDate detectionDate;

    @Column(name = "report_deadline")
    private LocalDate reportDeadline;

    // 연관 거래 ID 목록 (JSON Array)
    @Column(name = "related_transaction_ids", columnDefinition = "jsonb")
    private String relatedTransactionIds;

    @Column(name = "total_amount", precision = 20, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "fiu_report_id", length = 50)
    private String fiuReportId;  // FIU 보고 후 채번

    @Column(name = "filed_at")
    private LocalDateTime filedAt;

    @Column(name = "investigator", length = 50)
    private String investigator;

    @Column(name = "summary", length = 2000)
    private String summary;

    public static AmlCase createCtr(UUID customerId, BigDecimal totalAmount, List<UUID> txnIds) {
        AmlCase amlCase = new AmlCase();
        amlCase.caseId = UUID.randomUUID();
        amlCase.customerId = customerId;
        amlCase.caseType = AmlCaseType.CTR;
        amlCase.status = AmlCaseStatus.DRAFT;
        amlCase.detectionDate = LocalDate.now();
        amlCase.reportDeadline = LocalDate.now().plusDays(30); // CTR: 30일 이내 보고
        amlCase.totalAmount = totalAmount;
        amlCase.relatedTransactionIds = txnIds.toString();
        return amlCase;
    }

    public static AmlCase createStr(UUID customerId, BigDecimal totalAmount, List<UUID> txnIds) {
        AmlCase amlCase = createCtr(customerId, totalAmount, txnIds);
        amlCase.caseType = AmlCaseType.STR;
        amlCase.reportDeadline = LocalDate.now().plusDays(14); // STR: 14일 이내 보고
        return amlCase;
    }

    public void markAsFiled(String fiuReportId) {
        this.fiuReportId = fiuReportId;
        this.status = AmlCaseStatus.FILED;
        this.filedAt = LocalDateTime.now();
    }
}