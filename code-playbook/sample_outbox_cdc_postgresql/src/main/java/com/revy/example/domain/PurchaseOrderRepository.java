package com.revy.example.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseOrderRepository
        extends JpaRepository<PurchaseOrder, Long>, PurchaseOrderQueryRepository {
}