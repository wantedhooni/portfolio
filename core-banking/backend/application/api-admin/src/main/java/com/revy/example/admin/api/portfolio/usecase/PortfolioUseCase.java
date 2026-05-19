package com.revy.example.admin.api.portfolio.usecase;

import com.revy.example.admin.api.portfolio.payload.PortfolioPayload;

public interface PortfolioUseCase {

    PortfolioPayload.ModelResponse getPortfolio(Long accountId);
}
