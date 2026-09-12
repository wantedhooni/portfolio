package com.revy.trading.adapter.output.persistence.entity

import com.revy.trading.adapter.output.persistence.entity.common.BaseEntity
import com.revy.trading.adapter.output.persistence.entity.enums.OutboxStatus
import jakarta.persistence.Entity
import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "outbox_event", indexes = [Index(name = "idx_outbox_status_created_at", columnList = "status, created_at")]
)
class OutboxEventEntity(
    @Column(name = "event_id", nullable = false, unique = true, updatable = false, columnDefinition = "uuid")
    val eventId: UUID,
    @Column(name = "aggregate_id", nullable = false, updatable = false, columnDefinition = "uuid")
    val aggregateId: UUID,
    @Column(name = "event_type", nullable = false, updatable = false)
    val eventType: String,
    @Column(nullable = false, columnDefinition = "jsonb", updatable = false)
    val payload: String,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: OutboxStatus,
    @Column(name = "published_at")
    var publishedAt: Instant? = null
) : BaseEntity() {}