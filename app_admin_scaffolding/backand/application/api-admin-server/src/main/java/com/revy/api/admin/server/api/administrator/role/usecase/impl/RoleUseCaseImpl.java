package com.revy.api.admin.server.api.administrator.role.usecase.impl;

import com.revy.api.admin.server.api.administrator.role.payload.RolePayload;
import com.revy.api.admin.server.api.administrator.role.usecase.RoleUseCase;
import com.revy.application.facade.administrator.role.RoleProcessor;
import com.revy.application.facade.administrator.role.RoleReader;
import com.revy.application.facade.administrator.role.dto.RoleReaderDto;
import com.revy.common.web.api.response.ApiPageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoleUseCaseImpl implements RoleUseCase {
    private final RoleProcessor roleProcessor;
    private final RoleReader roleReader;

    @Override
    public RolePayload.Res create(RolePayload.CreateCommandReq req) {
        UUID roleId = roleProcessor.createRole(req.name(), req.description());
        return get(roleId);
    }

    @Override
    public RolePayload.Res get(UUID roleId) {
        RoleReaderDto.RoleView role = roleReader.getRoleViewById(roleId)
                                                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 역할입니다."));
        return toResponse(role);
    }

    @Override
    public ApiPageResponse<RolePayload.Res> getPage(int page, int size, String sortBy, String sortDirection,
                                                    String paramQuery) {
        RoleReaderDto.RolePage result = roleReader.getPage(page, size);
        return ApiPageResponse.of(result.content().stream().map(this::toResponse).toList(), result.totalElements(),
                                  result.page(), result.size());
    }

    @Override
    public RolePayload.Res update(UUID id, RolePayload.UpdateCommandReq req) {
        roleProcessor.updateRole(id, req.name(), req.description());
        return get(id);
    }

    @Override
    public RolePayload.Res addAdmin(RolePayload.AddAdminCommandReq req) {
        UUID roleId = UUID.fromString(req.roleId()), adminId = UUID.fromString(req.adminId());
        roleProcessor.addAdminToRole(roleId, adminId);
        return get(roleId);
    }

    @Override
    public void delete(UUID id) {
        roleProcessor.deleteRole(id);
    }

    private RolePayload.Res toResponse(RoleReaderDto.RoleView role) {
        List<RolePayload.AdminRef> adminRefs = role.admins()
                                                   .stream()
                                                   .map(admin -> {
                                                       return new RolePayload.AdminRef(admin.id().toString(), admin.email());
                                                   }).toList();
        return new RolePayload.Res(role.id().toString(), role.name(), role.description(), adminRefs, role.createdAt(),
                                   role.updatedAt());
    }
}
