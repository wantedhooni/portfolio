package com.revy.api_server.application.web.api.trade;

import com.revy.api_server.application.web.api.trade.payload.OrderPayload;
import com.revy.api_server.application.web.api.trade.usecase.OrderUseCase;
import com.revy.api_server.application.infra.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderApi {

    private final OrderUseCase orderUseCase;

    @PostMapping("/create")
    public OrderPayload.Res create(@AuthenticationPrincipal UserPrincipal currentUser,
                                   @RequestBody @Valid OrderPayload.Req req) {
        log.debug("Create order: {}", req);
        return orderUseCase.createOrder(currentUser.getId(),req);
    }



    /**
     * 주문 취소
     */
        @PostMapping("/{orderId}/cancel")
        public OrderPayload.Res cancel(
                @AuthenticationPrincipal UserPrincipal currentUser,
                @PathVariable UUID orderId) {
            return orderUseCase.cancelOrder(orderId);
        }

    /**
     * 계좌별 주문 목록(페이징)
     */
    @PostMapping
    public Page<OrderPayload.Res> listByAccount(@AuthenticationPrincipal UserPrincipal currentUser,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return orderUseCase.listOrders(currentUser.getId(), pageable);
    }




    /**
     * 체결 반영
     * - 거래소 체결 이벤트를 수신했다고 가정하고, 주문에 체결을 누적 반영한다.
     */
//    @PostMapping("/{orderId}/fills")
//    public FillResponse applyFill(@PathVariable UUID orderId, @Valid @RequestBody CreateFillRequest req) {
//        return orderService.applyFill(orderId, req);
//    }




    /**
     * 주문별 체결 목록(페이징)
     */
//    @GetMapping("/{orderId}/fills")
//    public Page<FillResponse> listFills(@PathVariable UUID orderId,
//                                        @RequestParam(defaultValue = "0") int page,
//                                        @RequestParam(defaultValue = "50") int size) {
//        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "executedAt"));
//        return orderService.listFillsByOrder(orderId, pageable);
//    }

}
