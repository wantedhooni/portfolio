package com.revy.example.domain;

import java.util.List;

public interface PurchaseOrderQueryRepository {

    List<PurchaseOrder> findByCustomerId(Long customerId);
}