package com.revy.securities.domain.trade.repo;

import com.revy.securities.domain.trade.OrderFill;
import com.revy.securities.domain.trade.repo.query.OrderFillQueryRepo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderFillRepo extends JpaRepository<OrderFill, UUID>, OrderFillQueryRepo {
}
