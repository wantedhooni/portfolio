package com.revy.example.admin.api.portfolio;

import com.revy.example.admin.api.common.ApiConstants;
import com.revy.example.admin.api.portfolio.payload.PortfolioPayload;
import com.revy.example.admin.api.portfolio.usecase.PortfolioUseCase;
import com.revy.example.core.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(ApiConstants.PREFIX_API_V1 + "/portfolio")
public class PortfolioController {

    private final PortfolioUseCase useCase;

    @GetMapping("/{accountId}")
    public ResponseEntity<ApiResponse<PortfolioPayload.ModelResponse>> get(@PathVariable Long accountId) {
        return ResponseEntity.ok(ApiResponse.ok(useCase.getPortfolio(accountId)));
    }
}
