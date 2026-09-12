package com.example.pension.domain

import com.example.pension.domain.shared.ContributionStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import jakarta.persistence.Version
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(
    name = "contribution",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_contribution_external_reference",
            columnNames = ["external_reference"]
        )
    ]
)
class Contribution protected constructor(
    @Column(name = "account_id", nullable = false, updatable = false)
    val accountId: Long,

    @Column(name = "external_reference", nullable = false, updatable = false, length = 100)
    val externalReference: String,

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    val amount: BigDecimal,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    var status: ContributionStatus,

    @Column(name = "received_at", nullable = false)
    val receivedAt: Instant,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
        protected set

    @Version
    var version: Long = 0
        protected set

    fun confirm() {
        require(status == ContributionStatus.RECEIVED) {
            "only RECEIVED contribution can be confirmed"
        }
        status = ContributionStatus.CONFIRMED
    }

    companion object {
        fun receive(accountId: Long, externalReference: String, amount: BigDecimal): Contribution {
            require(amount > BigDecimal.ZERO)
            return Contribution(
                accountId = accountId,
                externalReference = externalReference,
                amount = amount,
                status = ContributionStatus.RECEIVED,
                receivedAt = Instant.now(),
            )
        }
    }
}
