package com.revy.example.saas.api.portfolio;

import com.revy.example.core.common.ApiResponse;
import com.revy.example.jwt.payload.JwtPrincipal;
import com.revy.example.saas.api.common.ApiConstants;
import com.revy.example.saas.api.portfolio.payload.PortfolioPayload;
import com.revy.example.saas.api.portfolio.usecase.PortfolioUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstants.PREFIX_API_V1 + "/accounts/{accountId}/portfolio")
public class PortfolioController {

    private final PortfolioUseCase useCase;

    @GetMapping
    public ApiResponse<PortfolioPayload.ModelResponse> get(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long accountId
    ) {
        return ApiResponse.ok(useCase.getPortfolio(principal.id(), accountId));
    }
}
