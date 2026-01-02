package com.revy.example.config.mariadb_repl;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * MariaDB 복제 구성에서 라우팅 데이터소스를 설정하는 클래스.
 */
@Configuration
@Profile("mariadb_repl")
@EnableTransactionManagement
public class DataSourceRoutingConfig {

    /**
     * Primary 데이터소스 프로퍼티를 생성한다.
     *
     * @return primary 데이터소스 프로퍼티
     */
    @Bean(name = "primaryDataSourceProperties")
    @ConfigurationProperties("spring.datasource.primary")
    public DataSourceProperties primaryDataSourceProperties() {
        return new DataSourceProperties();
    }

    /**
     * Replica 데이터소스 프로퍼티를 생성한다.
     *
     * @return replica 데이터소스 프로퍼티
     */
    @Bean(name = "replicaDataSourceProperties")
    @ConfigurationProperties("spring.datasource.replica")
    public DataSourceProperties replicaDataSourceProperties() {
        return new DataSourceProperties();
    }

    /**
     * Primary 데이터소스를 생성한다.
     *
     * @param properties primary 데이터소스 프로퍼티
     * @return primary 데이터소스
     */
    @Bean
    @ConfigurationProperties("spring.datasource.primary.hikari")
    public HikariDataSource primaryDataSource(@Qualifier("primaryDataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }

    /**
     * Replica 데이터소스를 생성한다.
     *
     * @param properties replica 데이터소스 프로퍼티
     * @return replica 데이터소스
     */
    @Bean
    @ConfigurationProperties("spring.datasource.replica.hikari")
    public HikariDataSource replicaDataSource(@Qualifier("replicaDataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }

    /**
     * 라우팅 데이터소스를 구성한다.
     *
     * @param primaryDataSource primary 데이터소스
     * @param replicaDataSource replica 데이터소스
     * @return 라우팅 데이터소스
     */
    @Bean
    public DataSource routingDataSource(@Qualifier("primaryDataSource") DataSource primaryDataSource, @Qualifier("replicaDataSource") DataSource replicaDataSource) {
        RoutingDataSource routingDataSource = new RoutingDataSource();
        Map<Object, Object> dataSourceMap = new HashMap<>();
        dataSourceMap.put(DataSourceKey.PRIMARY, primaryDataSource);
        dataSourceMap.put(DataSourceKey.SECONDARY, replicaDataSource);
        routingDataSource.setTargetDataSources(dataSourceMap);
        routingDataSource.setDefaultTargetDataSource(primaryDataSource);
        return routingDataSource;
    }

    /**
     * 라우팅 데이터소스를 Lazy 프록시로 감싸 기본 데이터소스로 등록한다.
     *
     * @param routingDataSource 라우팅 데이터소스
     * @return 기본 데이터소스
     */
    @Bean
    @Primary
    public DataSource dataSource(@Qualifier("routingDataSource") DataSource routingDataSource) {
        return new LazyConnectionDataSourceProxy(routingDataSource);
    }
}