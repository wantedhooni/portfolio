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
@Tag(name = "Insurance - Claim", description = "보험금 청구 관리")
@RequiredArgsConstructor
@RestController
@RequestMapping(ApiConstants.PREFIX_API_V1 + "/insurance/claim")
public class InsuranceClaimController {

    private final InsuranceUseCase useCase;

    @Operation(summary = "보험금 청구 접수")
    @PostMapping
    public ResponseEntity<ApiResponse<InsurancePayload.ClaimResponse>> submit(
            @Valid @RequestBody InsurancePayload.SubmitClaimRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ok(useCase.submitClaim(request)));
    }

    @Operation(summary = "청구 단건 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InsurancePayload.ClaimResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(useCase.getClaim(id)));
    }

    @Operation(summary = "청구 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<ApiPageResponse<InsurancePayload.ClaimResponse>>> search(
            Pageable pageable,
            @Valid @ModelAttribute InsurancePayload.ClaimSearchRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(useCase.searchClaims(pageable, request)));
    }

    @Operation(summary = "심사 시작")
    @PostMapping("/{id}/start-review")
    public ResponseEntity<ApiResponse<Void>> startReview(
            @PathVariable Long id,
            @RequestParam Long reviewerAdminId
    ) {
        useCase.startClaimReview(id, reviewerAdminId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @Operation(summary = "청구 승인")
    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<Void>> approve(
            @PathVariable Long id,
            @Valid @RequestBody InsurancePayload.ApproveClaimRequest request
    ) {
        useCase.approveClaim(id, request);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @Operation(summary = "청구 거절")
    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<Void>> reject(
            @PathVariable Long id,
            @Valid @RequestBody InsurancePayload.RejectClaimRequest request
    ) {
        useCase.rejectClaim(id, request);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @Operation(summary = "보험금 지급 실행")
    @PostMapping("/{id}/pay")
    public ResponseEntity<ApiResponse<Void>> pay(
            @PathVariable Long id,
            @Valid @RequestBody InsurancePayload.PayClaimRequest request
    ) {
        useCase.payClaim(id, request);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
