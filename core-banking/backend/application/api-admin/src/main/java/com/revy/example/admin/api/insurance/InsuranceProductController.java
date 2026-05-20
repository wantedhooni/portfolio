package com.revy.example.admin.api.insurance;

import com.revy.example.admin.api.common.AbstractCrudApi;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "Insurance - Product", description = "보험 상품 관리")
@RequiredArgsConstructor
@RestController
@RequestMapping(ApiConstants.PREFIX_API_V1 + "/insurance/product")
public class InsuranceProductController extends AbstractCrudApi<
        Long,
        InsurancePayload.CreateProductRequest,
        InsurancePayload.UpdateProductPricingRequest,
        InsurancePayload.ProductSearchRequest,
        InsurancePayload.ProductResponse> {

    private final InsuranceUseCase useCase;

    @Override
    protected ApiPageResponse<InsurancePayload.ProductResponse> getPage(
            Pageable pageable, InsurancePayload.ProductSearchRequest request) {
        return useCase.searchProducts(pageable, request);
    }

    @Override
    protected InsurancePayload.ProductResponse doCreate(InsurancePayload.CreateProductRequest request) {
        return useCase.createProduct(request);
    }

    @Override
    protected InsurancePayload.ProductResponse doGet(Long id) {
        return useCase.getProduct(id);
    }

    @Override
    protected InsurancePayload.ProductResponse doUpdate(Long id,
                                                        InsurancePayload.UpdateProductPricingRequest request) {
        useCase.updateProductPricing(id, request);
        return useCase.getProduct(id);
    }

    @Override
    protected void doDelete(Long id) {
        useCase.discontinueProduct(id);
    }

    @Operation(summary = "보험 상품 판매 중단")
    @PostMapping("/{id}/discontinue")
    public ResponseEntity<ApiResponse<Void>> discontinue(@PathVariable Long id) {
        useCase.discontinueProduct(id);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
