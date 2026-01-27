package com.revy.api_server.domain.trade.repo;

import com.revy.api_server.domain.trade.OrderFill;
import com.revy.api_server.domain.trade.repo.query.OrderFillQueryRepo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderFillRepo extends JpaRepository<OrderFill, UUID>, OrderFillQueryRepo {
}
