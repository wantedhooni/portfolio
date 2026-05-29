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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "PG - Settlement", description = "PG 정산 관리 (배치 생성, 원장 전기)")
@RestController
@RequestMapping(ApiConstants.PREFIX_API_V1 + "/pg/settlement")
@RequiredArgsConstructor
public class PgSettlementController {

    private final PgUseCase useCase;

    @Operation(summary = "PG 정산 목록 조회")
    @GetMapping
    public ApiPageResponse<PgPayload.SettlementResponse> search(
            Pageable pageable,
            @ModelAttribute PgPayload.SettlementSearchRequest req) {
        return useCase.searchPgSettlements(pageable, req);
    }

    @Operation(summary = "PG 정산 단건 조회")
    @GetMapping("/{id}")
    public ApiResponse<PgPayload.SettlementResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(useCase.getPgSettlement(id));
    }

    @Operation(summary = "PG 정산 실패 처리")
    @PostMapping("/{id}/fail")
    public ApiResponse<Void> fail(
            @PathVariable Long id,
            @RequestBody @Valid PgPayload.FailSettlementRequest req) {
        useCase.failPgSettlement(id, req);
        return ApiResponse.ok();
    }
}
