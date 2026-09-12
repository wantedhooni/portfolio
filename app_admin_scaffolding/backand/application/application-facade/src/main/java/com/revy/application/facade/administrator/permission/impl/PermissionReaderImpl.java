package com.revy.application.facade.administrator.permission.impl;


import com.revy.application.facade.administrator.permission.PermissionReader;
import com.revy.application.facade.administrator.permission.dto.PermissionReaderDto;
import com.revy.domain.admin.AdminPermission;
import com.revy.domain.admin.QAdminPermission;
import com.revy.domain.admin.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PermissionReaderImpl implements PermissionReader {
    private final PermissionRepository permissionRepository;

    @Override
    public boolean existsByCode(String code) {
        return permissionRepository.exists(QAdminPermission.adminPermission.code.eq(code));
    }

    @Override
    public Optional<PermissionReaderDto.PermissionView> getPermissionViewById(UUID permissionId) {
        return permissionRepository.findOne(QAdminPermission.adminPermission.id.eq(permissionId)).map(this::toView);
    }

    @Override
    public PermissionReaderDto.PermissionPage getPage(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.max(size, 1);
        PageRequest pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AdminPermission> result = permissionRepository.findAll(QAdminPermission.adminPermission.id.isNotNull(), pageable);
        return new PermissionReaderDto.PermissionPage(
                result.getContent().stream().map(this::toView).toList(),
                result.getTotalElements(),
                safePage,
                safeSize
        );
    }

    private PermissionReaderDto.PermissionView toView(AdminPermission permission) {
        return new PermissionReaderDto.PermissionView(
                permission.getId(),
                permission.getCode(),
                permission.getDescription(),
                permission.getCreatedAt(),
                permission.getUpdatedAt()
        );
    }
}
