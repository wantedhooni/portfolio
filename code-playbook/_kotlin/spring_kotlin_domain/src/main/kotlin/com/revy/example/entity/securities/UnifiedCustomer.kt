package com.revy.example.entity.securities

import com.revy.example.entity.common.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "TB_UNIFIED_CUSTOMER")
class UnifiedCustomer protected constructor(

    @Column(name = "CUSTOMER_NO", length = 20)
    val customerNo: String

) : BaseEntity() {
    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    val contracts: MutableList<Contract> = mutableListOf()

    companion object {
        fun of(customerNo: String): UnifiedCustomer = UnifiedCustomer(customerNo)
    }
}

