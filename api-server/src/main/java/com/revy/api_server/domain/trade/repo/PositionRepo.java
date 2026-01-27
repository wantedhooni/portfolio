package com.revy.api_server.domain.trade.repo;

import com.revy.api_server.domain.trade.Trade;
import com.revy.api_server.domain.trade.repo.query.PositionQueryRepo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PositionRepo extends JpaRepository<Trade, UUID>, PositionQueryRepo {
}
