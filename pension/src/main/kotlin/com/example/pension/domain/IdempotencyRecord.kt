package com.example.pension.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.Instant
import java.util.*

@Entity
@Table(
    name = "idempotency_record",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_idempotency_operation_key",
            columnNames = ["operation", "idempotency_key"]
        )
    ]
)
class IdempotencyRecord protected constructor(
    @Column(name = "operation", nullable = false, length = 80)
    val operation: String,

    @Column(name = "idempotency_key", nullable = false, length = 200)
    val idempotencyKey: String,

    @Column(name = "request_hash", nullable = false, length = 64)
    val requestHash: String,

    @Column(name = "response_status")
    var responseStatus: Int?,

    @Column(name = "response_body", columnDefinition = "text")
    var responseBody: String?,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
        protected set

    fun complete(status: Int, body: String) {
        responseStatus = status
        responseBody = body
    }

    companion object {
        fun begin(operation: String, key: String, hash: String) =
            IdempotencyRecord(
                operation = operation,
                idempotencyKey = key,
                requestHash = hash,
                responseStatus = null,
                responseBody = null,
                createdAt = Instant.now(),
            )
    }
}