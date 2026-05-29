package com.revy.example.admin.api.pg;

import com.revy.example.admin.api.common.ApiConstants;
import com.revy.example.admin.api.pg.payload.PgPayload;
import com.revy.example.admin.api.pg.usecase.PgUseCase;
import com.revy.example.core.common.ApiPageResponse;
import com.revy.example.core.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "PG - Merchant", description = "PG 가맹점 관리")
@RestController
@RequestMapping(ApiConstants.PREFIX_API_V1 + "/pg/merchant")
@RequiredArgsConstructor
public class PgMerchantController {

    private final PgUseCase useCase;

    @Operation(summary = "가맹점 목록 조회")
    @GetMapping
    public ApiPageResponse<PgPayload.MerchantResponse> search(
            Pageable pageable,
            @ModelAttribute PgPayload.MerchantSearchRequest req) {
        return useCase.searchMerchants(pageable, req);
    }

    @Operation(summary = "가맹점 단건 조회")
    @GetMapping("/{id}")
    public ApiResponse<PgPayload.MerchantResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(useCase.getMerchant(id));
    }

    @Operation(summary = "가맹점 등록")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PgPayload.MerchantResponse> create(
            @RequestBody @Valid PgPayload.CreateMerchantRequest req) {
        return ApiResponse.ok(useCase.createMerchant(req));
    }

    @Operation(summary = "수수료율 수정")
    @PatchMapping("/{id}/commission")
    public ApiResponse<PgPayload.MerchantResponse> updateCommission(
            @PathVariable Long id,
            @RequestBody @Valid PgPayload.UpdateCommissionRequest req) {
        return ApiResponse.ok(useCase.updateCommission(id, req));
    }

    @Operation(summary = "가맹점 비활성화")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable Long id) {
        useCase.deactivateMerchant(id);
    }

    @Operation(summary = "가맹점 재활성화")
    @PostMapping("/{id}/activate")
    public ApiResponse<Void> activate(@PathVariable Long id) {
        useCase.activateMerchant(id);
        return ApiResponse.ok();
    }
}
