package com.revy.example.entity

import com.revy.example.entity.common.BaseEntity
import com.revy.example.entity.enums.MemberStatus
import jakarta.persistence.Entity
import jakarta.persistence.Table


@Entity
@Table(name = "members")
class Member(
    var email: String,
    var name: String,
    var status: MemberStatus = MemberStatus.ACTIVE
) : BaseEntity() {

    fun updateName(newName: String) {
        this.name = newName
    }

    fun deactivate() {
        this.status = MemberStatus.INACTIVE
    }
}