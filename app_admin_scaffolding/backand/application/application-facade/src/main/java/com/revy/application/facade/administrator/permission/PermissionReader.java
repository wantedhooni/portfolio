package com.revy.application.facade.administrator.permission;

import com.revy.application.facade.administrator.permission.dto.PermissionReaderDto;

import java.util.Optional;
import java.util.UUID;

public interface PermissionReader {
    boolean existsByCode(String code);

    Optional<PermissionReaderDto.PermissionView> getPermissionViewById(UUID permissionId);

    PermissionReaderDto.PermissionPage getPage(int page, int size);
}
