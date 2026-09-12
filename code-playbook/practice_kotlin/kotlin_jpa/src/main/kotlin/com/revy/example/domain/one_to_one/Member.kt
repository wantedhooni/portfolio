package com.revy.example.domain

import com.revy.example.domain.common.BaseEntity
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table

/**
 * 1:1 sample
 */
@Entity
@Table(name = "member")
class Member(
    @Column(nullable = false, unique = true)
    var email: String,
    @OneToOne(mappedBy = "member", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var profile: MemberProfile? = null
) : BaseEntity(
) {
    override fun toString(): String {
        return "Member(email='$email', profile=$profile) ${super.toString()}"
    }

}

@Entity
@Table(name = "member_profile")
class MemberProfile(
    @Column(name="nickname")
    var nickname: String,
    @Column(name="introduction")
    var introduction: String? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", unique = true)
    var member: Member? = null
)
    : BaseEntity() {
    override fun toString(): String {
        return "MemberProfile(nickname='$nickname', introduction=$introduction) ${super.toString()}"
    }
}