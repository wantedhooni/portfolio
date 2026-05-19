package com.revy.example.admin.api.portfolio.usecase.impl;

import com.revy.example.admin.api.portfolio.payload.PortfolioPayload;
import com.revy.example.admin.api.portfolio.usecase.PortfolioUseCase;
import com.revy.example.portfolio.reader.PortfolioReader;
import com.revy.example.portfolio.reader.dto.PortfolioPositionResult;
import com.revy.example.portfolio.reader.dto.PortfolioResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PortfolioUseCaseImpl implements PortfolioUseCase {

    private final PortfolioReader portfolioReader;

    @Override
    public PortfolioPayload.ModelResponse getPortfolio(Long accountId) {
        PortfolioResult portfolio = portfolioReader.getPortfolio(accountId);
        return new PortfolioPayload.ModelResponse(
            portfolio.accountId(),
            portfolio.balance(),
            portfolio.availableBalance(),
            portfolio.totalRealizedPnl(),
            portfolio.totalUnrealizedPnl(),
            portfolio.positions().stream().map(this::toPositionResponse).toList()
        );
    }

    private PortfolioPayload.PositionResponse toPositionResponse(PortfolioPositionResult position) {
        return new PortfolioPayload.PositionResponse(
            position.stockId(),
            position.ticker(),
            position.stockName(),
            position.totalQuantity(),
            position.averageBuyPrice(),
            position.currentPrice(),
            position.realizedPnl(),
            position.unrealizedPnl()
        );
    }
}
