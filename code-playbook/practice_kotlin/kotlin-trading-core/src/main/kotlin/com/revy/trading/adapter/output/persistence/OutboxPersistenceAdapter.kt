package com.revy.trading.adapter.output.persistence

import com.revy.trading.application.port.output.OutboxPort
import com.revy.trading.adapter.output.persistence.mapper.toOutboxEventEntity
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Component
class OutboxPersistenceAdapter(
    private val entityManager: EntityManager,
) : OutboxPort {
    @Transactional
    override fun append(aggregateId: UUID, eventType: String, payload: String) {
        require(eventType.isNotBlank())
        require(payload.isNotBlank())
        entityManager.persist(toOutboxEventEntity(aggregateId, eventType, payload))
    }
}
