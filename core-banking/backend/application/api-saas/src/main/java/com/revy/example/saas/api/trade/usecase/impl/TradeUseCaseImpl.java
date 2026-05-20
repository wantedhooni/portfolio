package com.revy.example.saas.api.trade.usecase.impl;

import com.revy.example.saas.api.common.AccountOwnershipValidator;
import com.revy.example.saas.api.trade.payload.TradePayload;
import com.revy.example.saas.api.trade.usecase.TradeUseCase;
import com.revy.example.trade.command.TradeCommand;
import com.revy.example.trade.command.dto.BuyCommand;
import com.revy.example.trade.command.dto.DividendCommand;
import com.revy.example.trade.command.dto.SellCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TradeUseCaseImpl implements TradeUseCase {

    private final TradeCommand tradeCommand;
    private final AccountOwnershipValidator ownershipValidator;

    @Override
    public void buy(Long userId, Long accountId, TradePayload.BuyRequest request) {
        ownershipValidator.requireOwner(userId, accountId);
        tradeCommand.buy(new BuyCommand(
            accountId,
            request.stockId(),
            request.quantity(),
            request.price(),
            request.fee(),
            request.tax(),
            request.referenceId(),
            request.tradedAt()
        ));
    }

    @Override
    public void sell(Long userId, Long accountId, TradePayload.SellRequest request) {
        ownershipValidator.requireOwner(userId, accountId);
        tradeCommand.sell(new SellCommand(
            accountId,
            request.stockId(),
            request.quantity(),
            request.price(),
            request.fee(),
            request.tax(),
            request.referenceId(),
            request.tradedAt()
        ));
    }

    @Override
    public void dividend(Long userId, Long accountId, TradePayload.DividendRequest request) {
        ownershipValidator.requireOwner(userId, accountId);
        tradeCommand.dividend(new DividendCommand(
            accountId,
            request.stockId(),
            request.grossAmount(),
            request.tax(),
            request.referenceId(),
            request.tradedAt()
        ));
    }
}
