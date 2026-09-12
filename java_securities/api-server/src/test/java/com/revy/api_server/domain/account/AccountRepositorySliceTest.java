package com.revy.api_server.domain.account;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.revy.securities.domain.account.Account;
import com.revy.securities.domain.account.enums.AccountType;
import com.revy.securities.domain.account.repo.AccountRepo;
import com.revy.securities.domain.account.QAccount;
import com.revy.common.enums.Currency;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AccountRepositorySliceTest.TestConfig.class)
@Transactional
@DirtiesContext
@DisplayName("Account Repository/Querydsl 슬라이스 테스트")
class AccountRepositorySliceTest {

    @Autowired
    private AccountRepo accountRepo;

    @Autowired
    private JPAQueryFactory jpaQueryFactory;

    @Test
    @DisplayName("계좌 저장 후 계좌번호로 조회된다")
    void saveAndFindByOwnerAndAccountNo() {
        Account account = Account.createNewAccount(1L, "09900010", AccountType.CASH, Currency.USD);
        Account saved = accountRepo.save(account);

        var found = accountRepo.findOneByOwnerIdAndAccountNo(1L, saved.getAccountNo());

        assertThat(found).isPresent();
        assertThat(found.get().getAccountNo()).isEqualTo(saved.getAccountNo());
    }

    @Test
    @DisplayName("Querydsl로 소유자 조건 조회가 가능하다")
    void querydslFindByOwner() {
        Account account = Account.createNewAccount(2L, "09900010", AccountType.CASH, Currency.USD);
        accountRepo.save(account);

        Account result = jpaQueryFactory.selectFrom(QAccount.account)
                .where(QAccount.account.ownerId.eq(2L))
                .fetchOne();

        assertThat(result).isNotNull();
        assertThat(result.getOwnerId()).isEqualTo(2L);
    }

    @Configuration
    @EnableJpaRepositories(basePackages = "com.revy.securities.domain.account.repo")
    @EnableTransactionManagement
    static class TestConfig {
        @Bean
        public DriverManagerDataSource dataSource() {
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setDriverClassName("org.h2.Driver");
            dataSource.setUrl("jdbc:h2:mem:testdb_account;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
            dataSource.setUsername("sa");
            dataSource.setPassword("");
            return dataSource;
        }

        @Bean
        public LocalContainerEntityManagerFactoryBean entityManagerFactory(DriverManagerDataSource dataSource) {
            initSchema(dataSource);
            LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
            factory.setDataSource(dataSource);
            factory.setPackagesToScan("com.revy.api_server.domain.account");
            HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
            adapter.setGenerateDdl(true);
            adapter.setShowSql(false);
            factory.setJpaVendorAdapter(adapter);
            java.util.Properties properties = new java.util.Properties();
            properties.setProperty("hibernate.hbm2ddl.auto", "none");
            properties.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
            properties.setProperty("hibernate.jdbc.time_zone", "UTC");
            properties.setProperty("hibernate.globally_quoted_identifiers", "true");
            properties.setProperty("hibernate.type.preferred_uuid_jdbc_type", "BINARY");
            factory.setJpaProperties(properties);
            return factory;
        }

        @Bean
        public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
            return new JpaTransactionManager(entityManagerFactory);
        }

        @Bean
        public JPAQueryFactory jpaQueryFactory(EntityManager entityManager) {
            return new JPAQueryFactory(entityManager);
        }

        private void initSchema(DriverManagerDataSource dataSource) {
            String[] ddl = {
                    "create table \"account\" ("
                            + "\"created_date\" timestamp(6), "
                            + "\"id\" bigint generated by default as identity, "
                            + "\"last_modified_date\" timestamp(6), "
                            + "\"publicId\" binary(16) not null unique, "
                            + "\"type\" varchar(16) not null, "
                            + "\"status\" varchar(20) not null, "
                            + "\"account_no\" varchar(50) not null unique, "
                            + "\"owner_id\" bigint not null, "
                            + "\"currency\" varchar(3) not null, "
                            + "\"cash_balance\" decimal(19,4) not null, "
                            + "\"available_cash\" decimal(19,4) not null, "
                            + "primary key (\"id\")"
                            + ")"
            };
            try (var conn = dataSource.getConnection(); var stmt = conn.createStatement()) {
                for (String sql : ddl) {
                    stmt.execute(sql);
                }
            } catch (Exception ex) {
                throw new IllegalStateException("schema init failed", ex);
            }
        }
    }
}
