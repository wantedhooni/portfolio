package com.revy.application.facade.administrator.role;

import java.util.UUID;

public interface RoleProcessor {
    UUID createRole(String name, String description);

    void updateRole(UUID roleId, String name, String description);

    void deleteRole(UUID roleId);

    void addAdminToRole(UUID roleId, UUID adminId);
}
