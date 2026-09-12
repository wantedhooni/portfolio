package com.revy.api.admin.server.init;

import com.revy.application.facade.administrator.admin.AdminReader;
import com.revy.application.facade.administrator.admin.InitAdminProcessor;
import com.revy.application.facade.user.UserProcessor;
import com.revy.application.facade.user.UserReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final PasswordEncoder passwordEncoder;

    private final AdminReader adminReader;
    private final InitAdminProcessor initAdminProcessor;
    private final UserReader userReader;
    private final UserProcessor userProcessor;

    @Value("${app.bootstrap.admin.email:sysadmin@system.dev}")
    private String bootstrapAdminEmail;

    @Value("${app.bootstrap.admin.password:qwer1234!}")
    private String bootstrapAdminPassword;

    @Value("${app.bootstrap.user.email:demo@demo.dev}")
    private String demoUserEmail;

    @Value("${app.bootstrap.user.password:qwer1234!}")
    private String demoUserPassword;

    @Override
    public void run(String... args) throws Exception {
        log.info("DataInitializer start");
        if (!adminReader.hasAnySecurityData()) {
            initAdminProcessor.initializeSecurityData(bootstrapAdminEmail,
                                                      passwordEncoder.encode(bootstrapAdminPassword));
        } else {
            log.info("security seed skipped: existing admin/role/permission data found");
        }

        if (!userReader.hasAnySecurityData()) {
            userProcessor.signup(demoUserEmail, passwordEncoder.encode(demoUserPassword), "first", "last", "DemoUser");
        }

        log.info("DataInitializer end");
    }
}
