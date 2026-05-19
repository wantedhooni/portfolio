package com.revy.example.trade.command.impl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.revy.example.account.reader.AccountReader;
import com.revy.example.domain.account.Account;
import com.revy.example.domain.account.AccountTx;
import com.revy.example.domain.account.QPositionLot;
import com.revy.example.domain.account.QStockPosition;
import com.revy.example.domain.account.Stock;
import com.revy.example.domain.account.StockPosition;
import com.revy.example.domain.account.exception.AccountNotFoundException;
import com.revy.example.domain.account.exception.InsufficientPositionException;
import com.revy.example.domain.account.exception.StockNotActiveException;
import com.revy.example.domain.account.exception.StockNotFoundException;
import com.revy.example.trade.command.TradeCommand;
import com.revy.example.trade.command.dto.BuyCommand;
import com.revy.example.trade.command.dto.DividendCommand;
import com.revy.example.trade.command.dto.SellCommand;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class TradeCommandImpl implements TradeCommand {

    private final EntityManager   entityManager;
    private final JPAQueryFactory jpaQueryFactory;
    private final AccountReader   accountReader;

    private final QStockPosition POSITION = QStockPosition.stockPosition;
    private final QPositionLot   LOT      = QPositionLot.positionLot;

    @Override
    public void buy(BuyCommand command) {
        // 1. 멱등성 체크
        if (accountReader.existsTxByReferenceId(command.referenceId())) {
            log.info("Duplicate buy ignored. referenceId={}", command.referenceId());
            return;
        }

        // 2. 계좌 / 종목 로딩
        Account account = loadAccount(command.accountId());
        Stock stock = loadStock(command.stockId());
        if (!stock.isActive()) {
            throw new StockNotActiveException();
        }

        // 3. 가용잔고 선점
        BigDecimal totalCost = command.totalCost();
        account.reserveForOrder(totalCost);

        // 4. 매수 원장 INSERT
        AccountTx buyTx = AccountTx.ofBuy(
            command.accountId(),
            command.stockId(),
            command.quantity(),
            command.price(),
            command.fee(),
            command.tax(),
            command.referenceId(),
            command.tradedAt()
        );
        entityManager.persist(buyTx);

        // 5. 포지션 조회 or 신규 생성
        StockPosition position = loadOrCreatePosition(command.accountId(), command.stockId());

        // 6. 로트 추가 (cascade로 자동 INSERT)
        position.addLot(buyTx.getId(), command.quantity(), command.price(), command.tradedAt());

        // 7. 실잔고 확정 차감
        account.confirmBuy(totalCost);
    }

    @Override
    public void sell(SellCommand command) {
        // 1. 멱등성 체크
        if (accountReader.existsTxByReferenceId(command.referenceId())) {
            log.info("Duplicate sell ignored. referenceId={}", command.referenceId());
            return;
        }

        // 2. 계좌 로딩
        Account account = loadAccount(command.accountId());

        // 3. 매도 원장 INSERT
        AccountTx sellTx = AccountTx.ofSell(
            command.accountId(),
            command.stockId(),
            command.quantity(),
            command.price(),
            command.fee(),
            command.tax(),
            command.referenceId(),
            command.tradedAt()
        );
        entityManager.persist(sellTx);

        // 4. FIFO 포지션 소진 (fetch join으로 활성 로트 일괄 로딩)
        StockPosition position = loadPositionWithLots(command.accountId(), command.stockId())
            .orElseThrow(InsufficientPositionException::new);

        position.sell(sellTx.getId(), command.quantity(), command.price(), command.tradedAt());

        // 5. 매도 대금 계좌 입금
        account.creditSaleProceeds(command.netProceeds());
    }

    @Override
    public void dividend(DividendCommand command) {
        if (accountReader.existsTxByReferenceId(command.referenceId())) {
            log.info("Duplicate dividend ignored. referenceId={}", command.referenceId());
            return;
        }

        Account account = loadAccount(command.accountId());

        entityManager.persist(
            AccountTx.ofDividend(
                command.accountId(),
                command.stockId(),
                command.grossAmount(),
                command.tax(),
                command.referenceId(),
                command.tradedAt()
            )
        );

        account.creditDividend(command.netAmount());
    }

    @Override
    public void releaseReservation(Long accountId, BigDecimal amount) {
        loadAccount(accountId).releaseReservation(amount);
    }

    // ── 내부 — 엔티티 로딩 ────────────────────────────────────────

    private Account loadAccount(Long accountId) {
        Account account = entityManager.find(Account.class, accountId);
        if (account == null) throw new AccountNotFoundException();
        return account;
    }

    private Stock loadStock(Long stockId) {
        Stock stock = entityManager.find(Stock.class, stockId);
        if (stock == null) throw new StockNotFoundException();
        return stock;
    }

    private StockPosition loadOrCreatePosition(Long accountId, Long stockId) {
        return loadPosition(accountId, stockId).orElseGet(() -> {
            StockPosition newPosition = StockPosition.create(accountId, stockId);
            entityManager.persist(newPosition);
            return newPosition;
        });
    }

    private Optional<StockPosition> loadPosition(Long accountId, Long stockId) {
        return Optional.ofNullable(
            jpaQueryFactory.selectFrom(POSITION)
                           .where(POSITION.accountId.eq(accountId).and(POSITION.stockId.eq(stockId)))
                           .fetchOne()
        );
    }

    /**
     * 매도용 — distinct + leftJoin fetchJoin으로 활성/비활성 로트 모두 로딩.
     * (JOIN FETCH WHERE 절로 lot 필터링은 Hibernate 컬렉션 무결성을 깨므로 도메인 measure activeLots()에 위임)
     */
    private Optional<StockPosition> loadPositionWithLots(Long accountId, Long stockId) {
        StockPosition position = jpaQueryFactory.selectFrom(POSITION).distinct()
            .leftJoin(POSITION.lots, LOT).fetchJoin()
            .where(POSITION.accountId.eq(accountId).and(POSITION.stockId.eq(stockId)))
            .fetchOne();
        return Optional.ofNullable(position);
    }
}
