package com.revy.example.insurance.reader.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.revy.example.domain.insurance.InsuranceClaim;
import com.revy.example.domain.insurance.InsurancePolicy;
import com.revy.example.domain.insurance.InsuranceProduct;
import com.revy.example.domain.insurance.PremiumPayment;
import com.revy.example.domain.insurance.QBeneficiary;
import com.revy.example.domain.insurance.QInsuranceClaim;
import com.revy.example.domain.insurance.QInsurancePolicy;
import com.revy.example.domain.insurance.QInsuranceProduct;
import com.revy.example.domain.insurance.QPremiumPayment;
import com.revy.example.domain.insurance.enums.PolicyStatus;
import com.revy.example.insurance.reader.InsuranceReader;
import com.revy.example.insurance.reader.dto.InsuranceClaimResult;
import com.revy.example.insurance.reader.dto.InsuranceClaimSearchCondition;
import com.revy.example.insurance.reader.dto.InsurancePolicyResult;
import com.revy.example.insurance.reader.dto.InsurancePolicySearchCondition;
import com.revy.example.insurance.reader.dto.InsuranceProductResult;
import com.revy.example.insurance.reader.dto.InsuranceProductSearchCondition;
import com.revy.example.insurance.reader.dto.PremiumPaymentResult;
import com.revy.example.utils.QuerydslUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class InsuranceReaderImpl implements InsuranceReader {

    private final JPAQueryFactory jpaQueryFactory;

    private final QInsuranceProduct PRODUCT = QInsuranceProduct.insuranceProduct;
    private final QInsurancePolicy  POLICY  = QInsurancePolicy.insurancePolicy;
    private final QBeneficiary      BEN     = QBeneficiary.beneficiary;
    private final QPremiumPayment   PAY     = QPremiumPayment.premiumPayment;
    private final QInsuranceClaim   CLAIM   = QInsuranceClaim.insuranceClaim;

    // ── Product ──────────────────────────────────────────────────

    @Override
    public Optional<InsuranceProductResult> findProductById(Long id) {
        InsuranceProduct p = jpaQueryFactory.selectFrom(PRODUCT).where(PRODUCT.id.eq(id)).fetchOne();
        return Optional.ofNullable(p).map(InsuranceProductResult::from);
    }

    @Override
    public Optional<InsuranceProductResult> findProductByCode(String productCode) {
        InsuranceProduct p = jpaQueryFactory.selectFrom(PRODUCT)
            .where(PRODUCT.productCode.eq(productCode)).fetchOne();
        return Optional.ofNullable(p).map(InsuranceProductResult::from);
    }

    @Override
    public Page<InsuranceProductResult> searchProducts(Pageable pageable, InsuranceProductSearchCondition condition) {
        BooleanBuilder where = new BooleanBuilder();
        if (condition != null) {
            where.and(QuerydslUtils.eq(PRODUCT.productCode, condition.getProductCode()));
            where.and(QuerydslUtils.like(PRODUCT.name, condition.getName()));
            where.and(QuerydslUtils.eq(PRODUCT.insuranceType, condition.getInsuranceType()));
            where.and(QuerydslUtils.eq(PRODUCT.currency, condition.getCurrency()));
            if (condition.getIsActive() != null) where.and(PRODUCT.isActive.eq(condition.getIsActive()));
        }

        List<InsuranceProduct> content = jpaQueryFactory.selectFrom(PRODUCT)
            .where(where).orderBy(PRODUCT.id.desc())
            .offset(pageable.getOffset()).limit(pageable.getPageSize()).fetch();

        JPAQuery<Long> countQuery = jpaQueryFactory.select(PRODUCT.count()).from(PRODUCT).where(where);

        return PageableExecutionUtils.getPage(
            content.stream().map(InsuranceProductResult::from).toList(),
            pageable, () -> Optional.ofNullable(countQuery.fetchOne()).orElse(0L)
        );
    }

    // ── Policy ───────────────────────────────────────────────────

    @Override
    public Optional<InsurancePolicyResult> findPolicyById(Long id) {
        InsurancePolicy p = jpaQueryFactory.selectFrom(POLICY).distinct()
            .leftJoin(POLICY.beneficiaries, BEN).fetchJoin()
            .where(POLICY.id.eq(id)).fetchOne();
        return Optional.ofNullable(p).map(InsurancePolicyResult::from);
    }

    @Override
    public Optional<InsurancePolicyResult> findPolicyByNumber(String policyNumber) {
        InsurancePolicy p = jpaQueryFactory.selectFrom(POLICY).distinct()
            .leftJoin(POLICY.beneficiaries, BEN).fetchJoin()
            .where(POLICY.policyNumber.eq(policyNumber)).fetchOne();
        return Optional.ofNullable(p).map(InsurancePolicyResult::from);
    }

    @Override
    public boolean existsPolicyByNumber(String policyNumber) {
        return jpaQueryFactory.selectOne().from(POLICY)
            .where(POLICY.policyNumber.eq(policyNumber)).fetchFirst() != null;
    }

    @Override
    public List<InsurancePolicyResult> findAllPoliciesByUserId(Long userId) {
        return jpaQueryFactory.selectFrom(POLICY).distinct()
            .leftJoin(POLICY.beneficiaries, BEN).fetchJoin()
            .where(POLICY.userId.eq(userId)).orderBy(POLICY.id.desc()).fetch()
            .stream().map(InsurancePolicyResult::from).toList();
    }

    @Override
    public Page<InsurancePolicyResult> searchPolicies(Pageable pageable, InsurancePolicySearchCondition condition) {
        BooleanBuilder where = new BooleanBuilder();
        if (condition != null) {
            where.and(QuerydslUtils.eq(POLICY.policyNumber, condition.getPolicyNumber()));
            where.and(QuerydslUtils.eq(POLICY.userId, condition.getUserId()));
            where.and(QuerydslUtils.eq(POLICY.insuredUserId, condition.getInsuredUserId()));
            where.and(QuerydslUtils.eq(POLICY.productId, condition.getProductId()));
            where.and(QuerydslUtils.eq(POLICY.status, condition.getStatus()));
        }

        List<InsurancePolicy> content = jpaQueryFactory.selectFrom(POLICY)
            .where(where).orderBy(POLICY.id.desc())
            .offset(pageable.getOffset()).limit(pageable.getPageSize()).fetch();

        JPAQuery<Long> countQuery = jpaQueryFactory.select(POLICY.count()).from(POLICY).where(where);

        return PageableExecutionUtils.getPage(
            content.stream().map(InsurancePolicyResult::from).toList(),
            pageable, () -> Optional.ofNullable(countQuery.fetchOne()).orElse(0L)
        );
    }

    @Override
    public List<InsurancePolicyResult> findPoliciesDueForBilling(LocalDate dueDate) {
        return jpaQueryFactory.selectFrom(POLICY)
            .where(POLICY.status.eq(PolicyStatus.ACTIVE)
                .and(POLICY.nextPaymentDate.loe(dueDate)))
            .orderBy(POLICY.nextPaymentDate.asc())
            .fetch().stream().map(InsurancePolicyResult::from).toList();
    }

    // ── Premium Payment ──────────────────────────────────────────

    @Override
    public Optional<PremiumPaymentResult> findPaymentById(Long id) {
        PremiumPayment p = jpaQueryFactory.selectFrom(PAY).where(PAY.id.eq(id)).fetchOne();
        return Optional.ofNullable(p).map(PremiumPaymentResult::from);
    }

    @Override
    public boolean existsPaymentByReferenceId(String referenceId) {
        return jpaQueryFactory.selectOne().from(PAY)
            .where(PAY.referenceId.eq(referenceId)).fetchFirst() != null;
    }

    @Override
    public List<PremiumPaymentResult> findAllPaymentsByPolicyId(Long policyId) {
        return jpaQueryFactory.selectFrom(PAY)
            .where(PAY.policyId.eq(policyId)).orderBy(PAY.dueDate.desc()).fetch()
            .stream().map(PremiumPaymentResult::from).toList();
    }

    // ── Claim ────────────────────────────────────────────────────

    @Override
    public Optional<InsuranceClaimResult> findClaimById(Long id) {
        InsuranceClaim c = jpaQueryFactory.selectFrom(CLAIM).where(CLAIM.id.eq(id)).fetchOne();
        return Optional.ofNullable(c).map(InsuranceClaimResult::from);
    }

    @Override
    public Optional<InsuranceClaimResult> findClaimByNumber(String claimNumber) {
        InsuranceClaim c = jpaQueryFactory.selectFrom(CLAIM)
            .where(CLAIM.claimNumber.eq(claimNumber)).fetchOne();
        return Optional.ofNullable(c).map(InsuranceClaimResult::from);
    }

    @Override
    public boolean existsClaimByNumber(String claimNumber) {
        return jpaQueryFactory.selectOne().from(CLAIM)
            .where(CLAIM.claimNumber.eq(claimNumber)).fetchFirst() != null;
    }

    @Override
    public Page<InsuranceClaimResult> searchClaims(Pageable pageable, InsuranceClaimSearchCondition condition) {
        BooleanBuilder where = new BooleanBuilder();
        if (condition != null) {
            where.and(QuerydslUtils.eq(CLAIM.claimNumber, condition.getClaimNumber()));
            where.and(QuerydslUtils.eq(CLAIM.policyId, condition.getPolicyId()));
            where.and(QuerydslUtils.eq(CLAIM.claimantUserId, condition.getClaimantUserId()));
            where.and(QuerydslUtils.eq(CLAIM.status, condition.getStatus()));
        }

        List<InsuranceClaim> content = jpaQueryFactory.selectFrom(CLAIM)
            .where(where).orderBy(CLAIM.id.desc())
            .offset(pageable.getOffset()).limit(pageable.getPageSize()).fetch();

        JPAQuery<Long> countQuery = jpaQueryFactory.select(CLAIM.count()).from(CLAIM).where(where);

        return PageableExecutionUtils.getPage(
            content.stream().map(InsuranceClaimResult::from).toList(),
            pageable, () -> Optional.ofNullable(countQuery.fetchOne()).orElse(0L)
        );
    }
}
