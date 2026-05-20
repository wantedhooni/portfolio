package com.revy.example.saas.api.trade.usecase;

import com.revy.example.saas.api.trade.payload.TradePayload;

public interface TradeUseCase {

    void buy(Long userId, Long accountId, TradePayload.BuyRequest request);

    void sell(Long userId, Long accountId, TradePayload.SellRequest request);

    void dividend(Long userId, Long accountId, TradePayload.DividendRequest request);
}
