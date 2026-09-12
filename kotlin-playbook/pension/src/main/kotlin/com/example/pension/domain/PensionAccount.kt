package com.example.pension.domain

import com.example.pension.domain.shared.AccountStatus
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "pension_account",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_pension_account_public_id",
            columnNames = ["public_id"]
        ),
        UniqueConstraint(
            name = "uk_pension_account_contract_participant",
            columnNames = ["contract_id", "participant_id"]
        )
    ]
)
class PensionAccount protected constructor(

    @Column(name = "public_id", nullable = false, updatable = false)
    val publicId: UUID,

    @Column(name = "contract_id", nullable = false, updatable = false)
    val contractId: Long,

    @Column(name = "participant_id", nullable = false, updatable = false)
    val participantId: Long,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    var status: AccountStatus,

    @Column(name = "cash_balance", nullable = false, precision = 19, scale = 2)
    var cashBalance: BigDecimal,

    @Column(name = "reserved_cash", nullable = false, precision = 19, scale = 2)
    var reservedCash: BigDecimal,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
        protected set

    @Version
    var version: Long = 0
        protected set

    val availableCash: BigDecimal
        get() = cashBalance.subtract(reservedCash)

    fun creditCash(amount: BigDecimal) {
        require(amount > BigDecimal.ZERO) { "amount must be positive" }
        cashBalance = cashBalance.add(amount)
        validateBalance()
    }

    fun reserveCash(amount: BigDecimal) {
        require(status == AccountStatus.OPEN) { "account is not open" }
        require(amount > BigDecimal.ZERO) { "amount must be positive" }
        if (availableCash < amount) {
            throw InsufficientCashException(availableCash, amount)
        }
        reservedCash = reservedCash.add(amount)
        validateBalance()
    }

    fun releaseReservedCash(amount: BigDecimal) {
        require(amount > BigDecimal.ZERO) { "amount must be positive" }
        require(reservedCash >= amount) { "reserved cash is insufficient" }
        reservedCash = reservedCash.subtract(amount)
        validateBalance()
    }

    fun settleBuy(amount: BigDecimal) {
        require(amount > BigDecimal.ZERO) { "amount must be positive" }
        require(reservedCash >= amount) { "reserved cash is insufficient" }
        require(cashBalance >= amount) { "cash balance is insufficient" }

        reservedCash = reservedCash.subtract(amount)
        cashBalance = cashBalance.subtract(amount)
        validateBalance()
    }

    private fun validateBalance() {
        require(cashBalance >= BigDecimal.ZERO)
        require(reservedCash >= BigDecimal.ZERO)
        require(cashBalance >= reservedCash)
    }

    companion object {
        fun open(contractId: Long, participantId: Long): PensionAccount =
            PensionAccount(
                publicId = UUID.randomUUID(),
                contractId = contractId,
                participantId = participantId,
                status = AccountStatus.OPEN,
                cashBalance = BigDecimal.ZERO.setScale(2),
                reservedCash = BigDecimal.ZERO.setScale(2),
                createdAt = Instant.now(),
            )
    }
}

class InsufficientCashException(
    val available: BigDecimal,
    val requested: BigDecimal,
) : RuntimeException("available cash is insufficient")