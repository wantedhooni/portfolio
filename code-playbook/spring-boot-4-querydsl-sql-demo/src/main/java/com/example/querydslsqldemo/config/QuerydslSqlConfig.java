package com.example.querydslsqldemo.config;

import com.querydsl.sql.Configuration;
import com.querydsl.sql.H2Templates;
import com.querydsl.sql.SQLQueryFactory;
import com.querydsl.sql.SQLTemplates;
import com.querydsl.sql.spring.SpringConnectionProvider;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;

@org.springframework.context.annotation.Configuration
public class QuerydslSqlConfig {

    @Bean
    public Configuration querydslSqlConfiguration() {
        SQLTemplates templates = H2Templates.builder()
                .printSchema()
                .build();

        Configuration configuration = new Configuration(templates);
        configuration.setUseLiterals(false);
        return configuration;
    }

    @Bean
    public SQLQueryFactory sqlQueryFactory(
            DataSource dataSource,
            Configuration querydslSqlConfiguration
    ) {
        return new SQLQueryFactory(
                querydslSqlConfiguration,
                new SpringConnectionProvider(dataSource)
        );
    }
}
