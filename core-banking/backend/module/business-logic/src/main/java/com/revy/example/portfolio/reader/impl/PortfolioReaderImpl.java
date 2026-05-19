package com.revy.example.portfolio.reader.impl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.revy.example.domain.account.Account;
import com.revy.example.domain.account.PositionLot;
import com.revy.example.domain.account.QPositionLot;
import com.revy.example.domain.account.QStock;
import com.revy.example.domain.account.QStockPosition;
import com.revy.example.domain.account.Stock;
import com.revy.example.domain.account.StockPosition;
import com.revy.example.domain.account.enums.LotStatus;
import com.revy.example.domain.account.exception.AccountNotFoundException;
import com.revy.example.portfolio.reader.PortfolioReader;
import com.revy.example.portfolio.reader.dto.PortfolioPositionResult;
import com.revy.example.portfolio.reader.dto.PortfolioResult;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PortfolioReaderImpl implements PortfolioReader {

    private static final int PRICE_SCALE = 4;

    private final EntityManager   entityManager;
    private final JPAQueryFactory jpaQueryFactory;

    private final QStockPosition POSITION = QStockPosition.stockPosition;
    private final QPositionLot   LOT      = QPositionLot.positionLot;
    private final QStock         STOCK    = QStock.stock;

    @Override
    public PortfolioResult getPortfolio(Long accountId) {
        Account account = entityManager.find(Account.class, accountId);
        if (account == null) throw new AccountNotFoundException();

        // 포지션 + 로트를 fetch join으로 일괄 로딩
        List<StockPosition> positions = jpaQueryFactory.selectFrom(POSITION).distinct()
            .leftJoin(POSITION.lots, LOT).fetchJoin()
            .where(POSITION.accountId.eq(accountId))
            .fetch();

        // 종목 정보를 한 번에 IN-쿼리로 로딩 (N+1 방지)
        List<Long> stockIds = positions.stream().map(StockPosition::getStockId).distinct().toList();
        Map<Long, Stock> stockMap = stockIds.isEmpty()
            ? Map.of()
            : loadStockMap(stockIds);

        List<PortfolioPositionResult> positionResults = positions.stream()
            .filter(p -> !p.isClosed())
            .map(p -> toPositionResult(p, stockMap.get(p.getStockId())))
            .toList();

        BigDecimal totalRealized = positions.stream()
            .map(StockPosition::getRealizedPnl)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalUnrealized = positionResults.stream()
            .map(PortfolioPositionResult::unrealizedPnl)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new PortfolioResult(
            account.getId(),
            account.getBalance(),
            account.getAvailableBalance(),
            totalRealized,
            totalUnrealized,
            positionResults
        );
    }

    private Map<Long, Stock> loadStockMap(List<Long> stockIds) {
        List<Stock> stocks = jpaQueryFactory.selectFrom(STOCK)
            .where(STOCK.id.in(stockIds))
            .fetch();
        Map<Long, Stock> map = new HashMap<>(stocks.size());
        for (Stock s : stocks) map.put(s.getId(), s);
        return map;
    }

    private PortfolioPositionResult toPositionResult(StockPosition position, Stock stock) {
        BigDecimal currentPrice = stock != null && stock.getLastPrice() != null
            ? stock.getLastPrice()
            : BigDecimal.ZERO;

        BigDecimal averageBuyPrice = computeAverageBuyPrice(position);
        BigDecimal unrealized = stock != null && stock.getLastPrice() != null
            ? position.unrealizedPnl(stock.getLastPrice())
            : BigDecimal.ZERO;

        return new PortfolioPositionResult(
            position.getStockId(),
            stock != null ? stock.getTicker() : null,
            stock != null ? stock.getName()   : null,
            position.getTotalQuantity(),
            averageBuyPrice,
            currentPrice,
            position.getRealizedPnl(),
            unrealized
        );
    }

    /** 잔여 수량 가중평균 매수단가 */
    private BigDecimal computeAverageBuyPrice(StockPosition position) {
        BigDecimal sumQty   = BigDecimal.ZERO;
        BigDecimal sumValue = BigDecimal.ZERO;

        for (PositionLot lot : position.getLots()) {
            if (lot.getLotStatus() == LotStatus.CLOSED) continue;
            sumQty   = sumQty.add(lot.getRemainingQuantity());
            sumValue = sumValue.add(lot.getRemainingQuantity().multiply(lot.getBuyPrice()));
        }

        if (sumQty.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return sumValue.divide(sumQty, PRICE_SCALE, RoundingMode.HALF_UP);
    }
}
