package com.revy.securities.domain.trade.repo;

import com.revy.securities.domain.trade.Trade;
import com.revy.securities.domain.trade.repo.query.PositionQueryRepo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PositionRepo extends JpaRepository<Trade, UUID>, PositionQueryRepo {
}
