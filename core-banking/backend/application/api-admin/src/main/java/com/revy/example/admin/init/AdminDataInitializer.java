package com.revy.example.admin;

import com.revy.example.admin.dto.RegisterAdminCommand;
import com.revy.example.domain.admin.AdminPermission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

@Component
public class AdminDataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminDataInitializer.class);

    private static final String ADMIN_DEMO_EMAIL    = "admin@example.com";
    private static final String ADMIN_DEMO_PASSWORD = "Qwer1234!";
    private static final String SUPER_ADMIN_ROLE    = "SUPER_ADMIN";

    private final AdminReader     adminReader;
    private final AdminCommand    adminCommand;
    private final RbacCommand     rbacCommand;
    private final RbacReader      rbacReader;
    private final PasswordEncoder passwordEncoder;

    public AdminDataInitializer(AdminReader adminReader,
                                AdminCommand adminCommand,
                                RbacCommand rbacCommand,
                                RbacReader rbacReader,
                                PasswordEncoder passwordEncoder) {
        this.adminReader     = adminReader;
        this.adminCommand    = adminCommand;
        this.rbacCommand     = rbacCommand;
        this.rbacReader      = rbacReader;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        // 슈퍼 어드민 역할 생성 (없을 경우)
        Long roleId;
        if (rbacReader.existsByName(SUPER_ADMIN_ROLE)) {
            roleId = rbacReader.findRoleByName(SUPER_ADMIN_ROLE).orElseThrow().id();
        } else {
            roleId = rbacCommand.createRole(SUPER_ADMIN_ROLE, "전체 권한을 가진 슈퍼 어드민",
                    EnumSet.allOf(AdminPermission.class));
            log.info("SUPER_ADMIN role created. id={}", roleId);
        }

        // 데모 어드민 계정 생성 (없을 경우)
        if (adminReader.existsByEmail(ADMIN_DEMO_EMAIL)) {
            return;
        }
        Long adminId = adminCommand.register(new RegisterAdminCommand(
            ADMIN_DEMO_EMAIL,
            passwordEncoder.encode(ADMIN_DEMO_PASSWORD),
            "관리자"
        ));
        rbacCommand.assignRole(adminId, roleId);
        log.info("Admin created and SUPER_ADMIN role assigned. adminId={}", adminId);
    }
}
