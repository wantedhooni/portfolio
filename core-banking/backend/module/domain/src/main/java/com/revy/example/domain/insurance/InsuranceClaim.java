package com.revy.example.domain.insurance;

import com.revy.example.domain.common.BaseEntity;
import com.revy.example.domain.insurance.enums.ClaimStatus;
import com.revy.example.domain.insurance.exception.ClaimAmountExceedsCoverageException;
import com.revy.example.domain.insurance.exception.ClaimNotPendingException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "insurance_claim",
    uniqueConstraints = @UniqueConstraint(name = "uq_claim_number", columnNames = "claim_number"),
    indexes = {
        @Index(name = "idx_claim_policy_id",  columnList = "policy_id"),
        @Index(name = "idx_claim_claimant",   columnList = "claimant_user_id"),
        @Index(name = "idx_claim_status",     columnList = "status"),
        @Index(name = "idx_claim_event_date", columnList = "event_date")
    }
)
public class InsuranceClaim extends BaseEntity {

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "claim_number", nullable = false, length = 30)
    private String claimNumber;

    @Column(name = "policy_id", nullable = false)
    private Long policyId;

    /** 청구인 (보통 피보험자 또는 수익자) */
    @Column(name = "claimant_user_id", nullable = false)
    private Long claimantUserId;

    /** 사고/진료 발생 일자 */
    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    @Column(name = "claim_reason", nullable = false, length = 500)
    private String claimReason;

    @Column(name = "claim_amount", nullable = false, precision = 20, scale = 2)
    private BigDecimal claimAmount;

    /** 심사 결과 승인 금액 (≤ claimAmount, ≤ policy.coverageAmount) */
    @Column(name = "approved_amount", precision = 20, scale = 2)
    private BigDecimal approvedAmount;

    /** 지급 계좌 */
    @Column(name = "payout_account_id")
    private Long payoutAccountId;

    /** 지급 시 생성된 AccountTx FK */
    @Column(name = "account_tx_id")
    private Long accountTxId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ClaimStatus status;

    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Column(name = "paid_at")
    private Instant paidAt;

    @Column(name = "reviewer_admin_id")
    private Long reviewerAdminId;

    @Column(name = "review_notes", length = 1000)
    private String reviewNotes;

    // ── 팩토리 ────────────────────────────────────────────────────
    public static InsuranceClaim submit(String claimNumber, Long policyId, Long claimantUserId,
                                        LocalDate eventDate, String claimReason, BigDecimal claimAmount,
                                        Long payoutAccountId, Instant now) {
        InsuranceClaim c = new InsuranceClaim();
        c.claimNumber       = claimNumber;
        c.policyId          = policyId;
        c.claimantUserId    = claimantUserId;
        c.eventDate         = eventDate;
        c.claimReason       = claimReason;
        c.claimAmount       = claimAmount;
        c.payoutAccountId   = payoutAccountId;
        c.status            = ClaimStatus.SUBMITTED;
        c.submittedAt       = now;
        return c;
    }

    // ── 비즈니스 ──────────────────────────────────────────────────
    public void startReview(Long reviewerAdminId) {
        if (this.status != ClaimStatus.SUBMITTED) {
            throw new ClaimNotPendingException();
        }
        this.reviewerAdminId = reviewerAdminId;
        this.status          = ClaimStatus.REVIEWING;
    }

    public void approve(BigDecimal approvedAmount, BigDecimal policyCoverage, String notes, Instant now) {
        if (this.status != ClaimStatus.REVIEWING && this.status != ClaimStatus.SUBMITTED) {
            throw new ClaimNotPendingException();
        }
        if (approvedAmount.compareTo(policyCoverage) > 0) {
            throw new ClaimAmountExceedsCoverageException(approvedAmount, policyCoverage);
        }
        this.approvedAmount = approvedAmount;
        this.reviewNotes    = notes;
        this.reviewedAt     = now;
        this.status         = ClaimStatus.APPROVED;
    }

    public void reject(String notes, Instant now) {
        if (this.status != ClaimStatus.REVIEWING && this.status != ClaimStatus.SUBMITTED) {
            throw new ClaimNotPendingException();
        }
        this.reviewNotes = notes;
        this.reviewedAt  = now;
        this.status      = ClaimStatus.REJECTED;
    }

    public void markPaid(Long accountTxId, Instant paidAt) {
        if (this.status != ClaimStatus.APPROVED) {
            throw new ClaimNotPendingException();
        }
        this.accountTxId = accountTxId;
        this.paidAt      = paidAt;
        this.status      = ClaimStatus.PAID;
    }
}
