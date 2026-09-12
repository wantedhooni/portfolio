package com.revy.example.config

import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import org.springframework.context.annotation.Configuration

@Configuration
class QuerydslConfig(private val entityManager: EntityManager) {

    fun jpaQueryFactory(): JPAQueryFactory{
        return JPAQueryFactory(entityManager);
    }
}