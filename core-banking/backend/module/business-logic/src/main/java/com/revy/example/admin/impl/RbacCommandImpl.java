package com.revy.example.admin.impl;

import com.revy.example.admin.RbacCommand;
import com.revy.example.admin.RbacReader;
import com.revy.example.core.error.BusinessException;
import com.revy.example.core.error.ErrorCode;
import com.revy.example.domain.admin.Admin;
import com.revy.example.domain.admin.AdminPermission;
import com.revy.example.domain.admin.AdminRole;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Component
@Transactional
@RequiredArgsConstructor
public class RbacCommandImpl implements RbacCommand {

    private final EntityManager entityManager;
    private final RbacReader    rbacReader;

    @Override
    public Long createRole(String name, String description, Set<AdminPermission> permissions) {
        if (rbacReader.existsByName(name)) {
            throw new BusinessException(ErrorCode.ROLE_DUPLICATED);
        }
        AdminRole role = AdminRole.create(name, description, permissions);
        entityManager.persist(role);
        return role.getId();
    }

    @Override
    public void updateRole(Long roleId, String name, String description, Set<AdminPermission> permissions) {
        AdminRole role = loadRole(roleId);
        if (!role.getName().equals(name) && rbacReader.existsByName(name)) {
            throw new BusinessException(ErrorCode.ROLE_DUPLICATED);
        }
        role.update(name, description, permissions);
    }

    @Override
    public void deleteRole(Long roleId) {
        entityManager.remove(loadRole(roleId));
    }

    @Override
    public void assignRole(Long adminId, Long roleId) {
        Admin admin = loadAdmin(adminId);
        AdminRole role = loadRole(roleId);
        if (admin.getRoles().contains(role)) {
            throw new BusinessException(ErrorCode.ROLE_ALREADY_ASSIGNED);
        }
        admin.assignRole(role);
    }

    @Override
    public void removeRole(Long adminId, Long roleId) {
        Admin admin = loadAdmin(adminId);
        AdminRole role = loadRole(roleId);
        if (!admin.getRoles().contains(role)) {
            throw new BusinessException(ErrorCode.ROLE_NOT_ASSIGNED);
        }
        admin.removeRole(role);
    }

    private Admin loadAdmin(Long adminId) {
        Admin admin = entityManager.find(Admin.class, adminId);
        if (admin == null) {
            throw new BusinessException(ErrorCode.ENTITY_NOT_FOUND);
        }
        return admin;
    }

    private AdminRole loadRole(Long roleId) {
        AdminRole role = entityManager.find(AdminRole.class, roleId);
        if (role == null) {
            throw new BusinessException(ErrorCode.ROLE_NOT_FOUND);
        }
        return role;
    }
}
