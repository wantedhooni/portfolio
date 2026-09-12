package com.revy.example.admin;

import com.revy.example.admin.reader.AdminReader;
import com.revy.example.doamin.admin.Admin;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Admin 서버의 로컬 실행과 데모 테스트에 필요한 기본 데이터를 준비합니다.
 */
@Component
public class AdminDataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminDataInitializer.class);

    private static final String ADMIN_DEMO_EMAIL = "admin@example.com";
    private static final String ADMIN_DEMO_PASSWORD = "Qwer1234!";

    private final EntityManager entityManager;
    private final AdminReader adminReader;
    private final PasswordEncoder passwordEncoder;

    public AdminDataInitializer(
            EntityManager entityManager, AdminReader adminReader, PasswordEncoder passwordEncoder) {
        this.entityManager = entityManager;
        this.adminReader = adminReader;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 데모 관리자가 없을 때 관리자 계정을 생성합니다.
     */
    @Override
    @Transactional
    public void run(ApplicationArguments args) {

        if (adminReader.findByEmail(ADMIN_DEMO_EMAIL).isEmpty()) {
            Admin admin = Admin.create(ADMIN_DEMO_EMAIL, passwordEncoder.encode(ADMIN_DEMO_PASSWORD), "관리자");
            entityManager.persist(admin);
            log.info("Admin Created: {}", admin);
        }

    }
}
