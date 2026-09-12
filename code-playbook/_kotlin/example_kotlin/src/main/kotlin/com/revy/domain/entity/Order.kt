package com.revy.domain.entity

import com.revy.domain.entity.common.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "order")
class Order : BaseEntity() {

}