package com.revy.example.saas.api.trade;

import com.revy.example.core.common.ApiResponse;
import com.revy.example.jwt.payload.JwtPrincipal;
import com.revy.example.saas.api.common.ApiConstants;
import com.revy.example.saas.api.trade.payload.TradePayload;
import com.revy.example.saas.api.trade.usecase.TradeUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstants.PREFIX_API_V1 + "/accounts/{accountId}/trades")
public class TradeController {

    private final TradeUseCase useCase;

    @PostMapping("/buy")
    public ApiResponse<Void> buy(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long accountId,
            @Valid @RequestBody TradePayload.BuyRequest request
    ) {
        useCase.buy(principal.id(), accountId, request);
        return ApiResponse.ok();
    }

    @PostMapping("/sell")
    public ApiResponse<Void> sell(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long accountId,
            @Valid @RequestBody TradePayload.SellRequest request
    ) {
        useCase.sell(principal.id(), accountId, request);
        return ApiResponse.ok();
    }

    @PostMapping("/dividend")
    public ApiResponse<Void> dividend(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long accountId,
            @Valid @RequestBody TradePayload.DividendRequest request
    ) {
        useCase.dividend(principal.id(), accountId, request);
        return ApiResponse.ok();
    }
}
