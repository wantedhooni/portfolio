package com.revy.example.entity.securities

import com.revy.example.entity.common.BaseEntity
import com.revy.example.entity.securities.enums.ContractType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "TB_CONTRACT")
class Contract protected constructor(

    @Column(name = "CONTRACT_NO", length = 20)
    val contractNo: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "CONTRACT_TYPE", nullable = false, length = 30)
    val contractType: ContractType,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CUSTOMER_NO", nullable = false)
    val customer: UnifiedCustomer

) : BaseEntity(){
    @OneToMany(mappedBy = "contract", fetch = FetchType.LAZY)
    val transactions: MutableList<CustomerTransaction> = mutableListOf()

    @OneToMany(mappedBy = "contract", fetch = FetchType.LAZY)
    val orders: MutableList<CustomerOrderExecution> = mutableListOf()

    companion object {
        fun of(contractNo: String, contractType: ContractType, customer: UnifiedCustomer): Contract =
            Contract(contractNo, contractType, customer)
    }
}