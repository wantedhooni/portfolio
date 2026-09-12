package com.revy.securities.domain.trade.repo;

import com.revy.securities.domain.trade.Order;
import com.revy.securities.domain.trade.repo.query.OrderQueryRepo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderRepo extends JpaRepository<Order, UUID>, OrderQueryRepo {
}
