package com.revy.example.admin.api.rbac.usecase.impl;

import com.revy.example.admin.RbacCommand;
import com.revy.example.admin.RbacReader;
import com.revy.example.admin.api.rbac.payload.RolePayload;
import com.revy.example.admin.api.rbac.usecase.RoleUseCase;
import com.revy.example.admin.dto.AdminRoleResult;
import com.revy.example.core.error.BusinessException;
import com.revy.example.core.error.ErrorCode;
import com.revy.example.domain.admin.AdminPermission;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoleUseCaseImpl implements RoleUseCase {

    private final RbacCommand rbacCommand;
    private final RbacReader  rbacReader;

    @Override
    public RolePayload.ModelResponse getRole(Long id) {
        AdminRoleResult result = rbacReader.findRoleById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROLE_NOT_FOUND));
        return map(result);
    }

    @Override
    public PageImpl<RolePayload.ModelResponse> search(Pageable pageable, RolePayload.SearchRequest request) {
        Page<AdminRoleResult> page = rbacReader.searchRoles(pageable, request.name());
        List<RolePayload.ModelResponse> content = page.getContent().stream().map(this::map).toList();
        return new PageImpl<>(content, pageable, page.getTotalElements());
    }

    @Override
    @Transactional
    public Long createRole(RolePayload.CreateRequest request) {
        return rbacCommand.createRole(request.name(), request.description(), toPermissions(request.permissions()));
    }

    @Override
    @Transactional
    public void updateRole(Long id, RolePayload.UpdateRequest request) {
        rbacCommand.updateRole(id, request.name(), request.description(), toPermissions(request.permissions()));
    }

    @Override
    @Transactional
    public void deleteRole(Long id) {
        rbacCommand.deleteRole(id);
    }

    @Override
    @Transactional
    public void assignRole(Long adminId, Long roleId) {
        rbacCommand.assignRole(adminId, roleId);
    }

    @Override
    @Transactional
    public void removeRole(Long adminId, Long roleId) {
        rbacCommand.removeRole(adminId, roleId);
    }

    private RolePayload.ModelResponse map(AdminRoleResult r) {
        return new RolePayload.ModelResponse(r.id(), r.name(), r.description(), r.permissions());
    }

    private Set<AdminPermission> toPermissions(Set<String> names) {
        if (names == null || names.isEmpty()) return EnumSet.noneOf(AdminPermission.class);
        return names.stream()
                .map(n -> {
                    try { return AdminPermission.valueOf(n); }
                    catch (IllegalArgumentException e) {
                        throw new BusinessException(ErrorCode.INVALID_INPUT);
                    }
                })
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(AdminPermission.class)));
    }
}
