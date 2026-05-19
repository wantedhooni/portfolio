package com.revy.example.stock.command.impl;

import com.revy.example.domain.account.Stock;
import com.revy.example.domain.account.exception.StockDuplicatedException;
import com.revy.example.domain.account.exception.StockNotFoundException;
import com.revy.example.stock.command.StockCommand;
import com.revy.example.stock.command.dto.CreateStockCommand;
import com.revy.example.stock.command.dto.UpdateMarketDataCommand;
import com.revy.example.stock.reader.StockReader;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@RequiredArgsConstructor
public class StockCommandImpl implements StockCommand {

    private final EntityManager entityManager;
    private final StockReader   stockReader;

    @Override
    public Long createStock(CreateStockCommand command) {
        if (stockReader.existsByTickerAndExchange(command.ticker(), command.exchange())) {
            throw new StockDuplicatedException(command.ticker(), command.exchange());
        }

        Stock stock = Stock.create(
            command.ticker(),
            command.name(),
            command.exchange(),
            command.sector(),
            command.currency()
        );
        entityManager.persist(stock);
        return stock.getId();
    }

    @Override
    public void updateMarketData(UpdateMarketDataCommand command) {
        loadStock(command.stockId()).updateMarketData(command.lastPrice(), command.marketCap());
    }

    @Override
    public void delist(Long stockId) {
        loadStock(stockId).delist();
    }

    private Stock loadStock(Long stockId) {
        Stock stock = entityManager.find(Stock.class, stockId);
        if (stock == null) {
            throw new StockNotFoundException();
        }
        return stock;
    }
}
