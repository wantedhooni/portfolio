package com.revy.application.facade.administrator.admin.impl;


import com.revy.application.facade.administrator.admin.AdminProcessor;
import com.revy.application.facade.administrator.admin.AdminReader;
import com.revy.application.facade.administrator.admin.dto.AdminReaderDto;
import com.revy.application.facade.administrator.admin.dto.CreateAdminDto;
import com.revy.common.domain.enums.admin.AdminStatus;
import com.revy.domain.admin.Admin;
import com.revy.domain.admin.AdminRole;
import com.revy.domain.admin.repository.AdminRepository;
import com.revy.domain.admin.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminProcessorImpl implements AdminProcessor {
    private static final String DEFAULT_ADMIN_ROLE_NAME = "ROLE_ADMIN";

    private final AdminReader adminReader;
    private final AdminRepository adminRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public CreateAdminDto.Result createAdmin(String email, String encodedPassword) {
        if (adminReader.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        AdminReaderDto.RoleRef roleRef = adminReader.getRoleByName(DEFAULT_ADMIN_ROLE_NAME)
                                                    .orElseGet(() -> {
                                                        AdminRole savedAdminRole = roleRepository.save(
                                                                new AdminRole(DEFAULT_ADMIN_ROLE_NAME));
                                                        return new AdminReaderDto.RoleRef(savedAdminRole.getId(),
                                                                                          savedAdminRole.getName());
                                                    });

        Admin admin = Admin.create(email, encodedPassword, AdminStatus.ACTIVE, true);
        admin.addRole(roleRepository.getReferenceById(roleRef.id()));
        adminRepository.save(admin);

        return new CreateAdminDto.Result(admin.getId().toString(), admin.getEmail());
    }

    @Override
    @Transactional
    public void update(UUID adminId,
                       String email,
                       String encodedPassword,
                       AdminStatus status,
                       Boolean enabled,
                       List<String> roleIds) {
        AdminReaderDto.AuthAdmin current = adminReader.getAuthAdminById(adminId)
                                                      .orElseThrow(
                                                              () -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        Admin admin = adminRepository.getReferenceById(adminId);

        if (email != null && !email.isBlank() && !email.equals(current.email())) {
            if (adminReader.existsByEmail(email)) {
                throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
            }
            admin.changeEmail(email);
        }
        if (encodedPassword != null && !encodedPassword.isBlank()) {
            admin.changePassword(encodedPassword);
        }
        if (status != null) {
            admin.changeStatus(status);
        }
        if (enabled != null) {
            admin.changeEnabled(enabled);
        }
        if (roleIds != null) {
            Set<UUID> parsedRoleIds = roleIds.stream()
                                             .map(UUID::fromString)
                                             .collect(Collectors.toSet());
            Set<UUID> currentRoleIds = admin.getAdminRoles().stream().map(AdminRole::getId).collect(Collectors.toSet());
            if (!currentRoleIds.equals(parsedRoleIds)) {
                admin.getAdminRoles().clear();
                if (!parsedRoleIds.isEmpty()) {
                    var roles = roleRepository.findAllById(parsedRoleIds);
                    if (roles.size() != parsedRoleIds.size()) {
                        throw new IllegalArgumentException("유효하지 않은 역할 ID가 포함되어 있습니다.");
                    }
                    roles.forEach(admin::addRole);
                }
            }
        }
        adminRepository.save(admin);
    }

    @Override
    @Transactional
    public void delete(UUID adminId) {
        if (adminReader.getAuthAdminById(adminId).isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다.");
        }
        adminRepository.deleteById(adminId);
    }
}
