package com.revy.example.admin;

import com.revy.example.admin.dto.RegisterAdminCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminDataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminDataInitializer.class);

    private static final String ADMIN_DEMO_EMAIL    = "admin@example.com";
    private static final String ADMIN_DEMO_PASSWORD = "Qwer1234!";

    private final AdminReader     adminReader;
    private final AdminCommand    adminCommand;
    private final PasswordEncoder passwordEncoder;

    public AdminDataInitializer(AdminReader adminReader,
                                AdminCommand adminCommand,
                                PasswordEncoder passwordEncoder) {
        this.adminReader     = adminReader;
        this.adminCommand    = adminCommand;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (adminReader.existsByEmail(ADMIN_DEMO_EMAIL)) {
            return;
        }
        Long id = adminCommand.register(new RegisterAdminCommand(
            ADMIN_DEMO_EMAIL,
            passwordEncoder.encode(ADMIN_DEMO_PASSWORD),
            "관리자"
        ));
        log.info("Admin Created. id={}", id);
    }
}
