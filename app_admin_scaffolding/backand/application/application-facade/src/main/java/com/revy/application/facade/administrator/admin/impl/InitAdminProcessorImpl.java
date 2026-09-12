package com.revy.application.facade.administrator.admin.impl;


import com.revy.application.facade.administrator.admin.InitAdminProcessor;
import com.revy.common.domain.enums.admin.AdminStatus;
import com.revy.domain.admin.Admin;
import com.revy.domain.admin.AdminPermission;
import com.revy.domain.admin.AdminRole;
import com.revy.domain.admin.repository.AdminRepository;
import com.revy.domain.admin.repository.PermissionRepository;
import com.revy.domain.admin.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class InitAdminProcessorImpl implements InitAdminProcessor {
    private static final String DEFAULT_ADMIN_ROLE_NAME = "ROLE_ADMIN";
    private static final List<String> DEFAULT_PERMISSION_CODES = List.of(
            "ADMIN:READ",
            "ADMIN:WRITE",
            "ADMIN:DELETE"
    );

    private final AdminRepository adminRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;



    @Override
    @Transactional
    public void initializeSecurityData(String email, String hashedPassword) {
        List<AdminPermission> permissions = permissionRepository.saveAll(
                DEFAULT_PERMISSION_CODES.stream().map(AdminPermission::new).toList()
        );

        AdminRole adminRole = new AdminRole(DEFAULT_ADMIN_ROLE_NAME);
        permissions.forEach(adminRole::addPermission);
        roleRepository.save(adminRole);

        Admin admin = Admin.create(
                email,
                hashedPassword,
                AdminStatus.ACTIVE,
                true
        );
        admin.addRole(adminRole);
        admin =  adminRepository.save(admin);
        log.warn("bootstrap admin created: email={}, role={}", admin.getEmail(), DEFAULT_ADMIN_ROLE_NAME);
    }
}
