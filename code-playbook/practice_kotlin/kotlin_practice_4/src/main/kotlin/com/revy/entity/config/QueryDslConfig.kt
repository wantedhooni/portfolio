package com.revy.entity.config

import com.querydsl.jpa.impl.JPAQueryFactory
import com.querydsl.sql.PostgreSQLTemplates
import com.querydsl.sql.SQLQueryFactory
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource


@Configuration
class QueryDslConfig(
    @PersistenceContext
    val em: EntityManager
) {

    @Bean
    fun jpaQueryFactory(): JPAQueryFactory {
        return JPAQueryFactory(em)
    }



    @Bean
    fun sqlQueryFactory(
        dataSource: DataSource
    ): SQLQueryFactory {
        // 1. 사용하는 DB 전용 템플릿 선택 (예: MySQL, PostgreSQLTemplates 등)
        val templates = PostgreSQLTemplates.builder().build()
        // 2. Querydsl SQL 설정 객체 생성
        val configuration =

            com.querydsl.sql.Configuration(templates)


        // 3. 빈으로 등록하여 전역에서 주입받을 수 있도록 리턴
        return SQLQueryFactory(configuration, dataSource)
    }
}
