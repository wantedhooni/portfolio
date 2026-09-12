package com.revy.example.entity.repository

import com.revy.example.entity.Member
import com.revy.example.entity.repository.dto.MemberSearchCondition

interface MemberRepositoryCustom {

    fun searchMembers(condition: MemberSearchCondition): List<Member>

}