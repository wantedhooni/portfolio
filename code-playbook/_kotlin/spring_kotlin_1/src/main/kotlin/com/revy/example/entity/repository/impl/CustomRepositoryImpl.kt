package com.revy.example.entity.repository.impl

import com.querydsl.jpa.impl.JPAQueryFactory
import com.revy.example.entity.repository.MemberRepositoryCustom

class CustomRepositoryImpl(
    private val jpaQueryFactory: JPAQueryFactory
) : MemberRepositoryCustom {
}