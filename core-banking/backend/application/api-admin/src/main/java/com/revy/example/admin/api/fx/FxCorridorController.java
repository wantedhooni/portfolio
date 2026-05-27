package com.revy.example.admin.api.fx;

import com.revy.example.admin.api.common.ApiConstants;
import com.revy.example.admin.api.fx.payload.FxPayload;
import com.revy.example.admin.api.fx.usecase.FxUseCase;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 관리자 FX 통화 회랑 API 컨트롤러입니다.
 *
 * <p>통화쌍별 환전 가능 금액, 일 한도, 스프레드율과 운영 상태를 관리합니다.</p>
 */
@Slf4j
@Tag(name = "FX - Corridor", description = "통화 회랑 관리")
@RequiredArgsConstructor
@RestController
@RequestMapping(ApiConstants.PREFIX_API_V1 + "/fx/corridor")
public class FxCorridorController {

    private final FxUseCase useCase;

    @Operation(summary = "통화 회랑 등록")
    @PostMapping
    public ResponseEntity<ApiResponse<FxPayload.CorridorResponse>> create(
            @Valid @RequestBody FxPayload.CorridorCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ok(useCase.createCorridor(request)));
    }

    @Operation(summary = "통화 회랑 단건 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FxPayload.CorridorResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(useCase.getCorridor(id)));
    }

    @Operation(summary = "통화 회랑 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<ApiPageResponse<FxPayload.CorridorResponse>>> search(
            Pageable pageable,
            @Valid @ModelAttribute FxPayload.CorridorSearchRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(useCase.searchCorridors(pageable, request)));
    }

    @Operation(summary = "통화 회랑 수정")
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<FxPayload.CorridorResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody FxPayload.CorridorUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(useCase.updateCorridor(id, request)));
    }

    @Operation(summary = "통화 회랑 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        useCase.deleteCorridor(id);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @Operation(summary = "통화 회랑 활성화")
    @PostMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<Void>> activate(@PathVariable Long id) {
        useCase.activateCorridor(id);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @Operation(summary = "통화 회랑 비활성화")
    @PostMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable Long id) {
        useCase.deactivateCorridor(id);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @Operation(summary = "통화 회랑 정지")
    @PostMapping("/{id}/suspend")
    public ResponseEntity<ApiResponse<Void>> suspend(@PathVariable Long id) {
        useCase.suspendCorridor(id);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
