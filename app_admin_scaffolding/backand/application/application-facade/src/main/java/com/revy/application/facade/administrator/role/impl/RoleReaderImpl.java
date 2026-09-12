package com.revy.application.facade.administrator.role.impl;


import com.revy.application.facade.administrator.role.RoleReader;
import com.revy.application.facade.administrator.role.dto.RoleReaderDto;
import com.revy.domain.admin.Admin;
import com.revy.domain.admin.AdminRole;
import com.revy.domain.admin.QAdminRole;
import com.revy.domain.admin.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoleReaderImpl implements RoleReader {
    private final RoleRepository roleRepository;

    @Override
    public boolean existsByName(String name) {
        return roleRepository.exists(QAdminRole.adminRole.name.eq(name));
    }

    @Override
    public Optional<RoleReaderDto.RoleView> getRoleViewById(UUID roleId) {
        return roleRepository.findOne(QAdminRole.adminRole.id.eq(roleId)).map(this::toView);
    }

    @Override
    public RoleReaderDto.RolePage getPage(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.max(size, 1);
        PageRequest pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AdminRole> result = roleRepository.findAll(QAdminRole.adminRole.id.isNotNull(), pageable);
        return new RoleReaderDto.RolePage(
                result.getContent().stream().map(this::toView).toList(),
                result.getTotalElements(),
                safePage,
                safeSize
        );
    }

    private RoleReaderDto.RoleView toView(AdminRole adminRole) {
        var admins = adminRole.getAdmins().stream()
                              .map(this::toAdminRef)
                              .sorted((left, right) -> left.email().compareToIgnoreCase(right.email()))
                              .collect(Collectors.toList());
        return new RoleReaderDto.RoleView(
                adminRole.getId(),
                adminRole.getName(),
                adminRole.getDescription(),
                admins,
                adminRole.getCreatedAt(),
                adminRole.getUpdatedAt()
        );
    }

    private RoleReaderDto.AdminRef toAdminRef(Admin admin) {
        return new RoleReaderDto.AdminRef(admin.getId(), admin.getEmail());
    }
}
