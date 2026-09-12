package com.revy.entity.common

import com.querydsl.jpa.impl.JPAQueryFactory
import com.querydsl.sql.SQLQueryFactory
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.beans.factory.annotation.Autowired

abstract class BaseEntityComponent {
    @PersistenceContext
    protected lateinit var em: EntityManager

    @Autowired
    protected lateinit var jpaQueryFactory: JPAQueryFactory


    @Autowired
    protected lateinit var sqlQueryFactory: SQLQueryFactory

}