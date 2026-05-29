package com.revy.example.insurance.reader;

import com.revy.example.insurance.reader.dto.InsuranceClaimResult;
import com.revy.example.insurance.reader.dto.InsuranceClaimSearchCondition;
import com.revy.example.insurance.reader.dto.InsurancePolicyResult;
import com.revy.example.insurance.reader.dto.InsurancePolicySearchCondition;
import com.revy.example.insurance.reader.dto.InsuranceProductResult;
import com.revy.example.insurance.reader.dto.InsuranceProductSearchCondition;
import com.revy.example.insurance.reader.dto.PremiumPaymentResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface InsuranceReader {

    // ── Product ──────────────────────────────────────────────────
    Optional<InsuranceProductResult> findProductById(Long id);
    Optional<InsuranceProductResult> findProductByCode(String productCode);
    Page<InsuranceProductResult> searchProducts(Pageable pageable, InsuranceProductSearchCondition condition);

    // ── Policy ───────────────────────────────────────────────────
    Optional<InsurancePolicyResult> findPolicyById(Long id);
    Optional<InsurancePolicyResult> findPolicyByNumber(String policyNumber);
    boolean existsPolicyByNumber(String policyNumber);
    List<InsurancePolicyResult> findAllPoliciesByUserId(Long userId);
    Page<InsurancePolicyResult> searchPolicies(Pageable pageable, InsurancePolicySearchCondition condition);
    /** 다음 납부일이 도래한 ACTIVE 계약 — 자동이체 배치용 */
    List<InsurancePolicyResult> findPoliciesDueForBilling(LocalDate dueDate);

    // ── Premium Payment ──────────────────────────────────────────
    Optional<PremiumPaymentResult> findPaymentById(Long id);
    boolean existsPaymentByReferenceId(String referenceId);
    List<PremiumPaymentResult> findAllPaymentsByPolicyId(Long policyId);
    /** 관리자 페이지용 범용 납부 내역 검색 */
    Page<PremiumPaymentResult> searchPayments(
            Pageable pageable, Long policyId, String status,
            LocalDate dueDateFrom, LocalDate dueDateTo);

    // ── Claim ────────────────────────────────────────────────────
    Optional<InsuranceClaimResult> findClaimById(Long id);
    Optional<InsuranceClaimResult> findClaimByNumber(String claimNumber);
    boolean existsClaimByNumber(String claimNumber);
    Page<InsuranceClaimResult> searchClaims(Pageable pageable, InsuranceClaimSearchCondition condition);
}
