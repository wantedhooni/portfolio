package com.revy.example.saas.api.portfolio.usecase;

import com.revy.example.saas.api.portfolio.payload.PortfolioPayload;

public interface PortfolioUseCase {

    PortfolioPayload.ModelResponse getPortfolio(Long userId, Long accountId);
}
