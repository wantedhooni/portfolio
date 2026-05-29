package com.revy.example.admin.api.insurance;

import com.revy.example.admin.api.common.ApiConstants;
import com.revy.example.admin.api.insurance.payload.InsurancePayload;
import com.revy.example.admin.api.insurance.usecase.InsuranceUseCase;
import com.revy.example.core.common.ApiPageResponse;
import com.revy.example.core.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "Insurance - Policy", description = "보험 증권 관리")
@RequiredArgsConstructor
@RestController
@RequestMapping(ApiConstants.PREFIX_API_V1 + "/insurance/policy")
public class InsurancePolicyController {

    private final InsuranceUseCase useCase;

    @Operation(summary = "보험 가입 (증권 발행)")
    @PostMapping
    public ResponseEntity<ApiResponse<InsurancePayload.PolicyResponse>> enroll(
            @Valid @RequestBody InsurancePayload.EnrollPolicyRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ok(useCase.enrollPolicy(request)));
    }

    @Operation(summary = "증권 단건 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InsurancePayload.PolicyResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(useCase.getPolicy(id)));
    }

    @Operation(summary = "증권 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<ApiPageResponse<InsurancePayload.PolicyResponse>>> search(
            Pageable pageable,
            @Valid @ModelAttribute InsurancePayload.PolicySearchRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(useCase.searchPolicies(pageable, request)));
    }

    @Operation(summary = "증권 활성화 (PENDING → ACTIVE)")
    @PostMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<Void>> activate(@PathVariable Long id) {
        useCase.activatePolicy(id);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @Operation(summary = "증권 정지")
    @PostMapping("/{id}/suspend")
    public ResponseEntity<ApiResponse<Void>> suspend(@PathVariable Long id) {
        useCase.suspendPolicy(id);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @Operation(summary = "증권 재활성화 (SUSPENDED → ACTIVE)")
    @PostMapping("/{id}/reactivate")
    public ResponseEntity<ApiResponse<Void>> reactivate(@PathVariable Long id) {
        useCase.reactivatePolicy(id);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @Operation(summary = "증권 만료 처리")
    @PostMapping("/{id}/terminate")
    public ResponseEntity<ApiResponse<Void>> terminate(@PathVariable Long id) {
        useCase.terminatePolicy(id);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @Operation(summary = "증권 취소")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancel(@PathVariable Long id) {
        useCase.cancelPolicy(id);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @Operation(summary = "보험료 납부 실행")
    @PostMapping("/{id}/pay-premium")
    public ResponseEntity<ApiResponse<Void>> payPremium(
            @PathVariable Long id,
            @Valid @RequestBody InsurancePayload.PayPremiumRequest request
    ) {
        useCase.payPremium(id, request);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @Operation(summary = "보험료 연체 처리")
    @PostMapping("/payment/{paymentId}/overdue")
    public ResponseEntity<ApiResponse<Void>> markOverdue(@PathVariable Long paymentId) {
        useCase.markPremiumOverdue(paymentId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @Operation(summary = "증권별 보험료 납부 내역 조회")
    @GetMapping("/{id}/payments")
    public ResponseEntity<ApiResponse<java.util.List<InsurancePayload.PremiumPaymentResponse>>> payments(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(useCase.getPaymentsByPolicy(id)));
    }
}
