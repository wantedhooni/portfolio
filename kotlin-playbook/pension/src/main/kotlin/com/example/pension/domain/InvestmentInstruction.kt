package com.example.pension.domain


import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import jakarta.persistence.Version
import java.math.BigDecimal

@Entity
@Table(
    name = "investment_instruction",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_instruction_account",
            columnNames = ["account_id"]
        )
    ]
)
class InvestmentInstruction protected constructor(
    @Column(name = "account_id", nullable = false)
    val accountId: Long,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
        protected set

    @Version
    var version: Long = 0
        protected set

    @OneToMany(
        mappedBy = "instruction",
        cascade = [CascadeType.PERSIST, CascadeType.REMOVE],
        orphanRemoval = true
    )
    private val _allocations: MutableList<InvestmentAllocation> = mutableListOf()

    val allocations: List<InvestmentAllocation>
        get() = _allocations.toList()

    fun replaceAllocations(items: List<AllocationValue>) {
        require(items.isNotEmpty())
        require(items.map { it.productCode }.distinct().size == items.size) {
            "duplicate product"
        }

        val total = items.fold(BigDecimal.ZERO) { acc, item -> acc + item.ratio }
        require(total.compareTo(BigDecimal("100.0000")) == 0) {
            "allocation total must be 100%"
        }

        _allocations.clear()
        items.forEach {
            require(it.ratio > BigDecimal.ZERO)
            _allocations += InvestmentAllocation(
                instruction = this,
                productCode = it.productCode,
                ratio = it.ratio,
            )
        }
    }
}

data class AllocationValue(
    val productCode: String,
    val ratio: BigDecimal,
)

@Entity
@Table(
    name = "investment_allocation",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_allocation_instruction_product",
            columnNames = ["instruction_id", "product_code"]
        )
    ]
)
class InvestmentAllocation(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "instruction_id", nullable = false)
    val instruction: InvestmentInstruction,

    @Column(name = "product_code", nullable = false, length = 50)
    val productCode: String,

    @Column(name = "ratio", nullable = false, precision = 7, scale = 4)
    val ratio: BigDecimal,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
        protected set
}