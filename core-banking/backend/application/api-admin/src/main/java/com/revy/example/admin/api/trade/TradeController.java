package com.revy.example.admin.api.trade;

import com.revy.example.admin.api.common.ApiConstants;
import com.revy.example.admin.api.trade.payload.TradePayload;
import com.revy.example.admin.api.trade.usecase.TradeUseCase;
import com.revy.example.core.common.ApiPageResponse;
import com.revy.example.core.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 체결 내역 조회 전용 컨트롤러 — 쓰기는 /api/v1/order 에서 처리 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(ApiConstants.PREFIX_API_V1 + "/trade")
public class TradeController {

    private final TradeUseCase useCase;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TradePayload.ModelResponse>> get(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(ApiResponse.ok(useCase.get(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ApiPageResponse<TradePayload.ModelResponse>>> search(
            Pageable pageable,
            @Valid @ModelAttribute TradePayload.SearchRequest searchRequest
    ) {
        return ResponseEntity.ok(ApiResponse.ok(useCase.search(pageable, searchRequest)));
    }
}
