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
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 보험료 납부 내역 관리 컨트롤러.
 *
 * <p>배치로 자동 생성된 납부 예정 레코드를 관리자가 직접 조회·처리할 수 있다.
 */
@Tag(name = "Insurance - Premium Payment", description = "보험료 납부 내역 관리")
@RestController
@RequestMapping(ApiConstants.PREFIX_API_V1 + "/insurance/premium-payment")
@RequiredArgsConstructor
public class InsurancePremiumController {

    private final InsuranceUseCase useCase;

    @Operation(summary = "보험료 납부 내역 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<ApiPageResponse<InsurancePayload.PremiumPaymentResponse>>> search(
            Pageable pageable,
            @ModelAttribute InsurancePayload.PremiumSearchRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                useCase.searchPremiumPayments(pageable, request)));
    }

    @Operation(summary = "보험료 납부 내역 단건 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InsurancePayload.PremiumPaymentResponse>> get(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(useCase.getPremiumPayment(id)));
    }

    @Operation(summary = "보험료 납부 예약 (관리자 수동 생성)")
    @PostMapping
    public ResponseEntity<ApiResponse<InsurancePayload.PremiumPaymentResponse>> schedule(
            @RequestBody @Valid InsurancePayload.SchedulePremiumRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(useCase.schedulePremiumPayment(request)));
    }

    @Operation(summary = "보험료 납부 실행")
    @PostMapping("/{id}/pay")
    public ResponseEntity<ApiResponse<Void>> pay(
            @PathVariable Long id,
            @RequestParam String referenceId) {
        useCase.payPremiumById(id, referenceId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @Operation(summary = "보험료 연체 처리")
    @PostMapping("/{id}/overdue")
    public ResponseEntity<ApiResponse<Void>> markOverdue(@PathVariable Long id) {
        useCase.markPremiumOverdue(id);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
