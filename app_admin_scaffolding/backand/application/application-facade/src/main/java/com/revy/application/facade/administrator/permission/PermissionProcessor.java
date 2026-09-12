package com.revy.application.facade.administrator.permission;

import java.util.UUID;

public interface PermissionProcessor {
    UUID createPermission(String code, String description);

    void updatePermission(UUID permissionId, String code, String description);

    void deletePermission(UUID permissionId);
}
