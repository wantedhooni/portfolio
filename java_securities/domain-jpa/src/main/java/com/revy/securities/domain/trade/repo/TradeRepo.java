package com.revy.securities.domain.trade.repo;

import com.revy.securities.domain.trade.Trade;
import com.revy.securities.domain.trade.repo.query.TradeQueryRepo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TradeRepo extends JpaRepository<Trade, UUID>, TradeQueryRepo {
}
