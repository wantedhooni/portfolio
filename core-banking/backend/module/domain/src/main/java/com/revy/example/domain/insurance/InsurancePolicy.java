package com.revy.example.domain.insurance;

import com.revy.example.domain.common.BaseEntity;
import com.revy.example.domain.insurance.enums.BeneficiaryType;
import com.revy.example.domain.insurance.enums.PolicyStatus;
import com.revy.example.domain.insurance.enums.PremiumFrequency;
import com.revy.example.domain.insurance.exception.InvalidBeneficiaryShareException;
import com.revy.example.domain.insurance.exception.PolicyExpiredException;
import com.revy.example.domain.insurance.exception.PolicyNotActiveException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "insurance_policy",
    uniqueConstraints = @UniqueConstraint(name = "uq_policy_number", columnNames = "policy_number"),
    indexes = {
        @Index(name = "idx_policy_user_id",    columnList = "user_id"),
        @Index(name = "idx_policy_product_id", columnList = "product_id"),
        @Index(name = "idx_policy_status",     columnList = "status"),
        @Index(name = "idx_policy_end_date",   columnList = "end_date")
    }
)
public class InsurancePolicy extends BaseEntity {

    /** 효력 발생 일자·상태 변경에 따른 동시성 충돌 방지 */
    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "policy_number", nullable = false, length = 30)
    private String policyNumber;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    /** 계약자 (보험료 납입 의무자) */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 피보험자 (계약자와 다를 수 있음 — 예: 부모가 자녀 보험 가입) */
    @Column(name = "insured_user_id", nullable = false)
    private Long insuredUserId;

    /** 보험료 출금 계좌 */
    @Column(name = "billing_account_id", nullable = false)
    private Long billingAccountId;

    @Column(name = "premium", nullable = false, precision = 18, scale = 2)
    private BigDecimal premium;

    @Enumerated(EnumType.STRING)
    @Column(name = "premium_frequency", nullable = false, length = 20)
    private PremiumFrequency premiumFrequency;

    @Column(name = "coverage_amount", nullable = false, precision = 20, scale = 2)
    private BigDecimal coverageAmount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    /** 다음 보험료 납부 예정일 */
    @Column(name = "next_payment_date")
    private LocalDate nextPaymentDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PolicyStatus status;

    @Column(name = "activated_at")
    private Instant activatedAt;

    @Column(name = "terminated_at")
    private Instant terminatedAt;

    @OneToMany(mappedBy = "policy", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("beneficiaryType ASC, id ASC")
    private List<Beneficiary> beneficiaries = new ArrayList<>();

    // ── 팩토리 ────────────────────────────────────────────────────
    public static InsurancePolicy enroll(String policyNumber, Long productId,
                                         Long userId, Long insuredUserId, Long billingAccountId,
                                         BigDecimal premium, PremiumFrequency frequency,
                                         BigDecimal coverageAmount, String currency,
                                         LocalDate startDate, LocalDate endDate) {
        InsurancePolicy p = new InsurancePolicy();
        p.policyNumber       = policyNumber;
        p.productId          = productId;
        p.userId             = userId;
        p.insuredUserId      = insuredUserId;
        p.billingAccountId   = billingAccountId;
        p.premium            = premium;
        p.premiumFrequency   = frequency;
        p.coverageAmount     = coverageAmount;
        p.currency           = currency;
        p.startDate          = startDate;
        p.endDate            = endDate;
        p.nextPaymentDate    = startDate;
        p.status             = PolicyStatus.PENDING;
        return p;
    }

    // ── 비즈니스 ──────────────────────────────────────────────────
    public void activate(Instant now) {
        if (this.status != PolicyStatus.PENDING) {
            throw new PolicyNotActiveException();
        }
        validateBeneficiariesShare();
        this.status = PolicyStatus.ACTIVE;
        this.activatedAt = now;
    }

    public void suspend() {
        validateActive();
        this.status = PolicyStatus.SUSPENDED;
    }

    public void reactivate() {
        if (this.status != PolicyStatus.SUSPENDED) {
            throw new PolicyNotActiveException();
        }
        this.status = PolicyStatus.ACTIVE;
    }

    public void terminate(Instant now) {
        if (this.status == PolicyStatus.TERMINATED || this.status == PolicyStatus.EXPIRED) {
            throw new PolicyExpiredException();
        }
        this.status = PolicyStatus.TERMINATED;
        this.terminatedAt = now;
    }

    public void cancel() {
        if (this.status != PolicyStatus.PENDING) {
            throw new PolicyNotActiveException();
        }
        this.status = PolicyStatus.CANCELLED;
    }

    /** 만기 도래 처리 (배치) */
    public void expireIfDue(LocalDate today) {
        if (this.status == PolicyStatus.ACTIVE && !today.isBefore(this.endDate)) {
            this.status = PolicyStatus.EXPIRED;
        }
    }

    /** 보험료 납부 후 다음 납부일 갱신 */
    public void advanceNextPaymentDate() {
        validateActive();
        this.nextPaymentDate = switch (this.premiumFrequency) {
            case MONTHLY    -> this.nextPaymentDate.plusMonths(1);
            case QUARTERLY  -> this.nextPaymentDate.plusMonths(3);
            case SEMIANNUAL -> this.nextPaymentDate.plusMonths(6);
            case ANNUAL     -> this.nextPaymentDate.plusYears(1);
            case ONE_TIME   -> null;  // 일시납은 더 이상 납부일 없음
        };
    }

    public Beneficiary addBeneficiary(Long beneficiaryUserId, String name, String relationship,
                                      BigDecimal sharePercent, BeneficiaryType type) {
        Beneficiary b = Beneficiary.create(this, beneficiaryUserId, name, relationship, sharePercent, type);
        this.beneficiaries.add(b);
        return b;
    }

    public void validateActive() {
        if (this.status != PolicyStatus.ACTIVE) {
            throw new PolicyNotActiveException();
        }
    }

    /** 1차 수익자 지분 합계가 100%인지 검증 */
    private void validateBeneficiariesShare() {
        BigDecimal sum = this.beneficiaries.stream()
            .filter(b -> b.getBeneficiaryType() == BeneficiaryType.PRIMARY)
            .map(Beneficiary::getSharePercent)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (sum.compareTo(new BigDecimal("100.00")) != 0) {
            throw new InvalidBeneficiaryShareException(sum);
        }
    }
}
