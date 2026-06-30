package com.revy.api_server.domain.trade.repo;

import com.revy.api_server.domain.trade.Order;
import com.revy.api_server.domain.trade.Trade;
import com.revy.api_server.domain.trade.repo.query.OrderQueryRepo;
import com.revy.api_server.domain.trade.repo.query.TradeQueryRepo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderRepo extends JpaRepository<Order, UUID>, OrderQueryRepo {
}
