package com.revy.example.saas.api.insurance;

import com.revy.example.core.common.ApiPageResponse;
import com.revy.example.core.common.ApiResponse;
import com.revy.example.jwt.payload.JwtPrincipal;
import com.revy.example.saas.api.common.ApiConstants;
import com.revy.example.saas.api.insurance.payload.InsurancePayload;
import com.revy.example.saas.api.insurance.usecase.InsuranceUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstants.PREFIX_API_V1 + "/insurance")
public class InsuranceController {

    private final InsuranceUseCase useCase;

    // ── Product (공개) ───────────────────────────────────────────

    @GetMapping("/products")
    public ApiResponse<ApiPageResponse<InsurancePayload.ProductResponse>> searchProducts(
            Pageable pageable,
            @Valid @ModelAttribute InsurancePayload.ProductSearchRequest request
    ) {
        return ApiResponse.ok(useCase.searchProducts(pageable, request));
    }

    @GetMapping("/products/{id}")
    public ApiResponse<InsurancePayload.ProductResponse> getProduct(@PathVariable Long id) {
        return ApiResponse.ok(useCase.getProduct(id));
    }

    // ── Policy (본인 전용) ───────────────────────────────────────

    @GetMapping("/policies")
    public ApiResponse<ApiPageResponse<InsurancePayload.PolicyResponse>> myPolicies(
            @AuthenticationPrincipal JwtPrincipal principal,
            Pageable pageable
    ) {
        return ApiResponse.ok(useCase.myPolicies(principal.id(), pageable));
    }

    @GetMapping("/policies/{id}")
    public ApiResponse<InsurancePayload.PolicyResponse> getMyPolicy(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long id
    ) {
        return ApiResponse.ok(useCase.getMyPolicy(principal.id(), id));
    }

    @PostMapping("/policies")
    public ResponseEntity<ApiResponse<InsurancePayload.PolicyResponse>> enroll(
            @AuthenticationPrincipal JwtPrincipal principal,
            @Valid @RequestBody InsurancePayload.EnrollRequest request
    ) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.ok(useCase.enroll(principal.id(), request)));
    }

    // ── Claim (본인 전용) ────────────────────────────────────────

    @GetMapping("/claims")
    public ApiResponse<ApiPageResponse<InsurancePayload.ClaimResponse>> myClaims(
            @AuthenticationPrincipal JwtPrincipal principal,
            Pageable pageable
    ) {
        return ApiResponse.ok(useCase.myClaims(principal.id(), pageable));
    }

    @GetMapping("/claims/{id}")
    public ApiResponse<InsurancePayload.ClaimResponse> getMyClaim(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long id
    ) {
        return ApiResponse.ok(useCase.getMyClaim(principal.id(), id));
    }

    @PostMapping("/claims")
    public ResponseEntity<ApiResponse<InsurancePayload.ClaimResponse>> submitClaim(
            @AuthenticationPrincipal JwtPrincipal principal,
            @Valid @RequestBody InsurancePayload.SubmitClaimRequest request
    ) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.ok(useCase.submitClaim(principal.id(), request)));
    }
}
