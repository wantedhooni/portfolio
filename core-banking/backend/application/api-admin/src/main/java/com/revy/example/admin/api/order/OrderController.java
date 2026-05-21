package com.revy.example.admin.api.order;

import com.revy.example.admin.api.common.ApiConstants;
import com.revy.example.admin.api.order.payload.OrderPayload;
import com.revy.example.admin.api.order.usecase.OrderUseCase;
import com.revy.example.core.common.ApiPageResponse;
import com.revy.example.core.common.ApiResponse;
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
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(ApiConstants.PREFIX_API_V1 + "/order")
public class OrderController {

    private final OrderUseCase useCase;

    /** 주문 접수 */
    @PostMapping
    public ResponseEntity<ApiResponse<OrderPayload.ModelResponse>> placeOrder(
            @Valid @RequestBody OrderPayload.PlaceRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                             .body(ApiResponse.ok(useCase.placeOrder(request)));
    }

    /** 주문 체결 */
    @PostMapping("/{id}/execute")
    public ResponseEntity<ApiResponse<OrderPayload.ModelResponse>> executeOrder(
            @PathVariable Long id,
            @Valid @RequestBody OrderPayload.ExecuteRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(useCase.executeOrder(id, request)));
    }

    /** 주문 취소 */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<OrderPayload.ModelResponse>> cancelOrder(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(ApiResponse.ok(useCase.cancelOrder(id)));
    }

    /** 주문 단건 조회 */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderPayload.ModelResponse>> get(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(ApiResponse.ok(useCase.get(id)));
    }

    /** 주문 목록 조회 */
    @GetMapping
    public ResponseEntity<ApiResponse<ApiPageResponse<OrderPayload.ModelResponse>>> search(
            Pageable pageable,
            @Valid @ModelAttribute OrderPayload.SearchRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(useCase.search(pageable, request)));
    }
}
