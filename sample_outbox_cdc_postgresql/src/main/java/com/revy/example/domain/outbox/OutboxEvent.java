package com.revy.example.domain.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "outboxevent")
public class OutboxEvent {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "aggregatetype", nullable = false)
    private String aggregateType;

    @Column(name = "aggregateid", nullable = false)
    private String aggregateId;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false, columnDefinition = "text")
    private String payload;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected OutboxEvent() {
    }

    private OutboxEvent(UUID id, String aggregateType, String aggregateId, String type, String payload) {
        this.id = id;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.type = type;
        this.payload = payload;
    }

    public static OutboxEvent orderCreated(Long orderId, String payload) {
        return new OutboxEvent(UUID.randomUUID(), "order", String.valueOf(orderId), "OrderCreated", payload);
    }

    public UUID getId() {
        return id;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public String getType() {
        return type;
    }

    public String getPayload() {
        return payload;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}