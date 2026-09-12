package com.revy.example.saas.init;

import com.revy.example.doamin.user.User;
import com.revy.example.user.reader.UserReader;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Admin 서버의 로컬 실행과 데모 테스트에 필요한 기본 데이터를 준비합니다.
 */
@Slf4j
@Component
public class UserDataInitializer implements ApplicationRunner {

    private static final String USER_DEMO_EMAIL = "demo@example.com";
    private static final String USER_DEMO_PASSWORD = "Qwer1234!";

    private final EntityManager entityManager;
    private final UserReader userReader;
    private final PasswordEncoder passwordEncoder;

    public UserDataInitializer(EntityManager entityManager, UserReader userReader, PasswordEncoder passwordEncoder) {
        this.entityManager = entityManager;
        this.userReader = userReader;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 데모 관리자가 없을 때 관리자 계정을 생성합니다.
     */
    @Override
    @Transactional
    public void run(ApplicationArguments args) {

        if (userReader.findByEmail(USER_DEMO_EMAIL).isEmpty()) {
            User user = User.create(USER_DEMO_EMAIL, passwordEncoder.encode(USER_DEMO_PASSWORD), "demoUser");
            entityManager.persist(user);
            log.info("user Created ID: {}", user.getId());
        }

    }
}




