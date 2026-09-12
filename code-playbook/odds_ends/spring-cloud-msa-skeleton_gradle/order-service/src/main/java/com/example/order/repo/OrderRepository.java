package com.example.order.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.order.entity.OrderEntity;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    List<OrderEntity> findByUserId(long userId);
}
