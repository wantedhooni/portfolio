package com.revy.example.saas.api.portfolio.usecase.impl;

import com.revy.example.portfolio.reader.PortfolioReader;
import com.revy.example.portfolio.reader.dto.PortfolioPositionResult;
import com.revy.example.portfolio.reader.dto.PortfolioResult;
import com.revy.example.saas.api.common.AccountOwnershipValidator;
import com.revy.example.saas.api.portfolio.payload.PortfolioPayload;
import com.revy.example.saas.api.portfolio.usecase.PortfolioUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PortfolioUseCaseImpl implements PortfolioUseCase {

    private final PortfolioReader portfolioReader;
    private final AccountOwnershipValidator ownershipValidator;

    @Override
    @Transactional(readOnly = true)
    public PortfolioPayload.ModelResponse getPortfolio(Long userId, Long accountId) {
        ownershipValidator.requireOwner(userId, accountId);
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
