package com.example.order.entity;

import java.time.Instant;

import jakarta.persistence.*;

@Entity
@Table(name = "orders")
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private long userId;

    @Column(nullable = false)
    private String symbol;

    @Column(nullable = false)
    private long quantity;

    @Column(nullable = false)
    private Instant createdAt;

    protected OrderEntity() {}

    public OrderEntity(long userId, String symbol, long quantity, Instant createdAt) {
        this.userId = userId;
        this.symbol = symbol;
        this.quantity = quantity;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public long getUserId() { return userId; }
    public String getSymbol() { return symbol; }
    public long getQuantity() { return quantity; }
    public Instant getCreatedAt() { return createdAt; }
}
