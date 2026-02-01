package com.revy.securities.domain.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuerydslConfig {

    /**
     * Querydsl에서 사용할 JPAQueryFactory를 제공한다.
     *
     * @param entityManager 엔티티 매니저
     * @return JPAQueryFactory
     */
    @Bean
    public JPAQueryFactory jpaQueryFactory(EntityManager entityManager) {
        return new JPAQueryFactory(entityManager);
    }
}
