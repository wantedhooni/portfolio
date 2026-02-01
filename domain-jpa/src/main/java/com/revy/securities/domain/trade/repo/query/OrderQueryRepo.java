package com.revy.securities.domain.trade.repo.query;

import com.revy.securities.domain.trade.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderQueryRepo {

    Page<Order> findOrdersByUserId(Long ownerId, Pageable pageable);
}
