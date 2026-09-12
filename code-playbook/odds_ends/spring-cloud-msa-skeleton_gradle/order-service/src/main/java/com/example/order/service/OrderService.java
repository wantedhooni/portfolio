package com.example.order.service;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.order.domain.CreateOrderRequest;
import com.example.order.entity.OrderEntity;
import com.example.order.repo.OrderRepository;

@Service
public class OrderService {

    private final OrderRepository repo;

    public OrderService(OrderRepository repo) {
        this.repo = repo;
    }

    public OrderEntity create(CreateOrderRequest req) {
        return repo.save(new OrderEntity(req.userId(), req.symbol(), req.quantity(), Instant.now()));
    }

    public List<OrderEntity> listByUser(long userId) {
        return repo.findByUserId(userId);
    }
}
