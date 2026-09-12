package com.revy.trading.adapter.output.persistence

import com.revy.trading.application.port.output.LedgerPort
import com.revy.trading.adapter.output.persistence.mapper.toEntities
import com.revy.trading.domain.ledger.LedgerTransaction
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class LedgerPersistenceAdapter(
    private val entityManager: EntityManager,
): LedgerPort {
    @Transactional
    override fun append(transaction: LedgerTransaction) {
        transaction.toEntities().forEach(entityManager::persist)
    }
}
