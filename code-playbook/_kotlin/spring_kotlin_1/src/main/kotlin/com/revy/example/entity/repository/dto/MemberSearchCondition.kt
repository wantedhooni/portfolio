package com.revy.example.entity.repository.dto

import com.revy.example.entity.enums.MemberStatus

data class MemberSearchCondition(
    val name: String? = null,
    val status: MemberStatus? = null,
)