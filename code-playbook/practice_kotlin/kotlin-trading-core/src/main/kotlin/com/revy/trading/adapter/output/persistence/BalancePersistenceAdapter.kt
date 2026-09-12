package com.revy.trading.adapter.output.persistence

import com.revy.trading.adapter.output.persistence.entity.AccountBalanceEntity
import com.revy.trading.adapter.output.persistence.mapper.toDomain
import com.revy.trading.adapter.output.persistence.mapper.toEntity
import com.revy.trading.application.port.output.BalancePort
import com.revy.trading.domain.balance.AccountBalance
import jakarta.persistence.EntityManager
import jakarta.persistence.LockModeType
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Component
class BalancePersistenceAdapter(
    private val entityManager: EntityManager,
) : BalancePort {

    @Transactional
    override fun findByAccountId(accountId: UUID): AccountBalance {
        return entityManager.createQuery(
            "select balance from AccountBalanceEntity balance where balance.accountId = :accountId",
            AccountBalanceEntity::class.java,
        )
            .setParameter("accountId", accountId)
            .setLockMode(LockModeType.PESSIMISTIC_WRITE)
            .singleResult
            .toDomain()
    }

    override fun save(balance: AccountBalance) {
        val entity = entityManager.createQuery(
            "select balance from AccountBalanceEntity balance where balance.accountId = :accountId",
            AccountBalanceEntity::class.java,
        )
            .setParameter("accountId", balance.accountId)
            .resultList
            .singleOrNull()

        if (entity == null) {
            entityManager.persist(balance.toEntity())
        } else {
            entity.availableAmount = balance.available().amount
            entity.reservedAmount = balance.reserved().amount
        }
    }
}
