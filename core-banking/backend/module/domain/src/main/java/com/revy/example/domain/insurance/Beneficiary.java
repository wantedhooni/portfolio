package com.revy.example.domain.insurance;

import com.revy.example.domain.common.BaseEntity;
import com.revy.example.domain.insurance.enums.BeneficiaryType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "beneficiary",
    indexes = {
        @Index(name = "idx_beneficiary_policy_id", columnList = "policy_id"),
        @Index(name = "idx_beneficiary_user_id",   columnList = "beneficiary_user_id")
    }
)
public class Beneficiary extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private InsurancePolicy policy;

    /** 수익자가 시스템 사용자인 경우. 외부인일 수도 있음 (null 허용) */
    @Column(name = "beneficiary_user_id")
    private Long beneficiaryUserId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /** 관계 (배우자, 자녀, 부모 등) */
    @Column(name = "relationship", nullable = false, length = 50)
    private String relationship;

    /** 지분율 (0~100, 소수점 2자리) — 1차 수익자 합 = 100 */
    @Column(name = "share_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal sharePercent;

    @Enumerated(EnumType.STRING)
    @Column(name = "beneficiary_type", nullable = false, length = 20)
    private BeneficiaryType beneficiaryType;

    // ── 팩토리 (package-private — InsurancePolicy를 통해서만 생성) ──
    static Beneficiary create(InsurancePolicy policy, Long beneficiaryUserId, String name,
                              String relationship, BigDecimal sharePercent, BeneficiaryType type) {
        Beneficiary b = new Beneficiary();
        b.policy            = policy;
        b.beneficiaryUserId = beneficiaryUserId;
        b.name              = name;
        b.relationship      = relationship;
        b.sharePercent      = sharePercent;
        b.beneficiaryType   = type;
        return b;
    }

    public void updateShare(BigDecimal sharePercent) {
        this.sharePercent = sharePercent;
    }
}
