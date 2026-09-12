package com.revy.api_server.domain.trade.repo;

import com.revy.api_server.domain.trade.Trade;
import com.revy.api_server.domain.trade.repo.query.TradeQueryRepo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TradeRepo extends JpaRepository<Trade, UUID>, TradeQueryRepo {
}
