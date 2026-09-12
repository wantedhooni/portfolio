package com.revy.api.admin.server.api.administrator.role.usecase;

import com.revy.api.admin.server.api.administrator.role.payload.RolePayload;
import com.revy.common.web.api.response.ApiPageResponse;

import java.util.UUID;

public interface RoleUseCase {
    RolePayload.Res create(RolePayload.CreateCommandReq req);

    RolePayload.Res get(UUID roleId);

    ApiPageResponse<RolePayload.Res> getPage(int page, int size, String sortBy, String sortDirection, String paramQuery);

    RolePayload.Res update(UUID id, RolePayload.UpdateCommandReq req);

    RolePayload.Res addAdmin(RolePayload.AddAdminCommandReq req);

    void delete(UUID id);
}
