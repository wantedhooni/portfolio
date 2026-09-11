package com.example.pension.domain


import org.springframework.data.domain.Page
import org.springframework.data.jpa.repository.JpaRepository
import java.awt.print.Pageable
import java.util.UUID

interface PensionAccountRepository : JpaRepository<PensionAccount, Long> {
    fun findByPublicId(publicId: UUID): PensionAccount?
    fun existsByContractIdAndParticipantId(contractId: Long, participantId: Long): Boolean
}

interface ContributionRepository : JpaRepository<Contribution, Long> {
    fun existsByExternalReference(externalReference: String): Boolean
}

interface InvestmentOrderRepository : JpaRepository<InvestmentOrder, Long> {
    fun findByClientOrderId(clientOrderId: String): InvestmentOrder?
    fun findAllByAccountId(accountId: Long, pageable: Pageable): Page<InvestmentOrder>
}

interface OrderExecutionRepository : JpaRepository<OrderExecution, Long> {
    fun existsByExternalExecutionId(externalExecutionId: String): Boolean
}

interface HoldingRepository : JpaRepository<Holding, Long> {
    fun findByAccountIdAndProductCode(accountId: Long, productCode: String): Holding?
    fun findAllByAccountId(accountId: Long): List<Holding>
}

interface LedgerEntryRepository : JpaRepository<LedgerEntry, Long>

interface OutboxEventRepository : JpaRepository<OutboxEvent, UUID>

interface IdempotencyRecordRepository : JpaRepository<IdempotencyRecord, Long> {
    fun findByOperationAndIdempotencyKey(operation: String, idempotencyKey: String): IdempotencyRecord?
}