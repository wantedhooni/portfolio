package com.revy.example.admin;

import com.revy.example.domain.admin.AdminPermission;

import java.util.Set;

public interface RbacCommand {

    Long createRole(String name, String description, Set<AdminPermission> permissions);

    void updateRole(Long roleId, String name, String description, Set<AdminPermission> permissions);

    void deleteRole(Long roleId);

    void assignRole(Long adminId, Long roleId);

    void removeRole(Long adminId, Long roleId);
}
