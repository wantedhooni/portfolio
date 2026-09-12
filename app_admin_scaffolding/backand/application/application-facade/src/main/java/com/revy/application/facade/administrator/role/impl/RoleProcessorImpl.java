package com.revy.application.facade.administrator.role.impl;

import com.revy.application.facade.administrator.role.RoleProcessor;
import com.revy.application.facade.administrator.role.RoleReader;
import com.revy.domain.admin.Admin;
import com.revy.domain.admin.AdminRole;
import com.revy.domain.admin.repository.AdminRepository;
import com.revy.domain.admin.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoleProcessorImpl implements RoleProcessor {
    private final RoleRepository roleRepository;
    private final AdminRepository adminRepository;
    private final RoleReader roleReader;

    @Override
    @Transactional
    public UUID createRole(String name, String description) {
        if (roleReader.existsByName(name)) {
            throw new IllegalArgumentException("이미 사용 중인 역할명입니다.");
        }
        AdminRole adminRole = AdminRole.create(name, description);
        roleRepository.save(adminRole);
        return adminRole.getId();
    }

    @Override
    @Transactional
    public void updateRole(UUID roleId, String name, String description) {
        AdminRole adminRole = roleRepository.findById(roleId)
                                            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 역할입니다."));

        if (name != null && !name.isBlank() && !name.equals(adminRole.getName())) {
            if (roleReader.existsByName(name)) {
                throw new IllegalArgumentException("이미 사용 중인 역할명입니다.");
            }
            adminRole.changeName(name);
        }
        if (description != null) {
            adminRole.changeDescription(description);
        }
        roleRepository.save(adminRole);
    }

    @Override
    @Transactional
    public void deleteRole(UUID roleId) {
        if (roleRepository.findById(roleId).isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 역할입니다.");
        }
        roleRepository.deleteById(roleId);
    }

    @Override
    @Transactional
    public void addAdminToRole(UUID roleId, UUID adminId) {
        AdminRole adminRole = roleRepository.findById(roleId)
                                            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 역할입니다."));
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        admin.addRole(adminRole);
        adminRepository.save(admin);
    }
}
