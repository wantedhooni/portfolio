package com.revy.api_server.domain.user;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.revy.securities.domain.user.Authority;
import com.revy.securities.domain.user.QUser;
import com.revy.securities.domain.user.Role;
import com.revy.securities.domain.user.User;
import com.revy.securities.domain.user.UserDetail;
import com.revy.securities.domain.user.UserStatus;
import com.revy.securities.domain.user.repo.AuthorityRepository;
import com.revy.securities.domain.user.repo.RoleRepository;
import com.revy.securities.domain.user.repo.UserDetailRepository;
import com.revy.securities.domain.user.repo.UserRepository;
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
@ContextConfiguration(classes = UserRepositorySliceTest.TestConfig.class)
@Transactional
@DirtiesContext
@DisplayName("User/Role/Authority Repository/Querydsl 슬라이스 테스트")
class UserRepositorySliceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserDetailRepository userDetailRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private JPAQueryFactory jpaQueryFactory;

    @Test
    @DisplayName("권한과 역할을 저장하고 조회한다")
    void saveRoleWithAuthority() {
        Authority authority = new Authority("USER_READ", "read");
        authorityRepository.save(authority);

        Role role = new Role("USER");
        role.addAuthority(authority);
        roleRepository.save(role);

        var found = roleRepository.findByName("USER");

        assertThat(found).isPresent();
        assertThat(found.get().getAuthorities()).isNotEmpty();
    }

    @Test
    @DisplayName("사용자와 상세정보를 저장하고 조회한다")
    void saveUserAndDetail() {
        User user = new User();
        user.setEmail("a@b.com");
        user.setPassword("pw");
        user.setStatus(UserStatus.ACTIVE);
        User savedUser = userRepository.save(user);

        UserDetail detail = UserDetail.builder()
                                      .user(savedUser)
                                      .name("name")
                                      .phone("010")
                                      .address("addr")
                                      .build();
        savedUser.setDetail(detail);
        userDetailRepository.save(detail);

        User found = userRepository.findById(savedUser.getId()).orElseThrow();

        assertThat(found.getEmail()).isEqualTo("a@b.com");
        assertThat(found.getDetail()).isNotNull();
    }

    @Test
    @DisplayName("Querydsl로 이메일 조건 조회가 가능하다")
    void querydslFindByEmail() {
        User user = new User();
        user.setEmail("q@b.com");
        user.setPassword("pw");
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        User result = jpaQueryFactory.selectFrom(QUser.user)
                .where(QUser.user.email.eq("q@b.com"))
                .fetchOne();

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("q@b.com");
    }

    @Configuration
    @EnableJpaRepositories(basePackages = "com.revy.securities.domain.user.repo")
    @EnableTransactionManagement
    static class TestConfig {
        @Bean
        public DriverManagerDataSource dataSource() {
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setDriverClassName("org.h2.Driver");
            dataSource.setUrl("jdbc:h2:mem:testdb_user;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
            dataSource.setUsername("sa");
            dataSource.setPassword("");
            return dataSource;
        }

        @Bean
        public LocalContainerEntityManagerFactoryBean entityManagerFactory(DriverManagerDataSource dataSource) {
            initSchema(dataSource);
            LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
            factory.setDataSource(dataSource);
            factory.setPackagesToScan("com.revy.api_server.domain.user");
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
                    "create table \"user\" ("
                            + "\"created_date\" timestamp(6), "
                            + "\"id\" bigint generated by default as identity, "
                            + "\"last_modified_date\" timestamp(6), "
                            + "\"publicId\" binary(16) not null unique, "
                            + "\"email\" varchar(255) not null unique, "
                            + "\"password\" varchar(255) not null, "
                            + "\"status\" varchar(20) not null, "
                            + "primary key (\"id\")"
                            + ")",
                    "create table \"user_detail\" ("
                            + "\"created_date\" timestamp(6), "
                            + "\"id\" bigint generated by default as identity, "
                            + "\"last_modified_date\" timestamp(6), "
                            + "\"publicId\" binary(16) not null unique, "
                            + "\"user_id\" bigint not null, "
                            + "\"name\" varchar(255) not null, "
                            + "\"phone\" varchar(255) not null, "
                            + "\"address\" varchar(255) not null, "
                            + "primary key (\"id\")"
                            + ")",
                    "create table \"role\" ("
                            + "\"created_date\" timestamp(6), "
                            + "\"id\" bigint generated by default as identity, "
                            + "\"last_modified_date\" timestamp(6), "
                            + "\"publicId\" binary(16) not null unique, "
                            + "\"name\" varchar(255) not null unique, "
                            + "primary key (\"id\")"
                            + ")",
                    "create table \"authoritiy\" ("
                            + "\"created_date\" timestamp(6), "
                            + "\"id\" bigint generated by default as identity, "
                            + "\"last_modified_date\" timestamp(6), "
                            + "\"publicId\" binary(16) not null unique, "
                            + "\"name\" varchar(255) not null unique, "
                            + "\"description\" varchar(255) not null, "
                            + "primary key (\"id\")"
                            + ")",
                    "create table \"user_role\" ("
                            + "\"user_id\" bigint not null, "
                            + "\"role_id\" bigint not null"
                            + ")",
                    "create table \"role_authoritiy\" ("
                            + "\"role_id\" bigint not null, "
                            + "\"authority_id\" bigint not null"
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
