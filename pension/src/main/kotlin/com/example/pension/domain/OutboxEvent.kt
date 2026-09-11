package com.example.pension.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.Instant
import java.util.*

@Entity
@Table(
    name = "outbox_event",
    indexes = [
        Index(
            name = "idx_outbox_unpublished",
            columnList = "published_at, created_at"
        )
    ]
)
class OutboxEvent protected constructor(
    @Column(name = "aggregate_type", nullable = false, length = 50)
    val aggregateType: String,

    @Column(name = "aggregate_id", nullable = false, length = 100)
    val aggregateId: String,

    @Column(name = "event_type", nullable = false, length = 100)
    val eventType: String,

    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    val payload: String,

    @Column(name = "occurred_at", nullable = false)
    val occurredAt: Instant,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant,

    @Column(name = "published_at")
    var publishedAt: Instant?,

    @Column(name = "retry_count", nullable = false)
    var retryCount: Int,
) {
    @Id
    val id: UUID = UUID.randomUUID()

    fun markPublished(now: Instant) {
        publishedAt = now
    }

    fun markFailed() {
        retryCount += 1
    }

    companion object {
        fun create(
            aggregateType: String,
            aggregateId: String,
            eventType: String,
            payload: String,
        ) = OutboxEvent(
            aggregateType = aggregateType,
            aggregateId = aggregateId,
            eventType = eventType,
            payload = payload,
            occurredAt = Instant.now(),
            createdAt = Instant.now(),
            publishedAt = null,
            retryCount = 0,
        )
    }
}