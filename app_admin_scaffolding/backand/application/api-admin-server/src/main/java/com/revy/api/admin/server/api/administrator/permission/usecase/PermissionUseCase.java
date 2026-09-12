package com.revy.api.admin.server.api.administrator.permission.usecase;

import com.revy.api.admin.server.api.administrator.permission.payload.PermissionPayload;
import com.revy.common.web.api.response.ApiPageResponse;

import java.util.UUID;

public interface PermissionUseCase {
    PermissionPayload.Res create(PermissionPayload.CreateCommandReq req);

    PermissionPayload.Res get(UUID permissionId);

    ApiPageResponse<PermissionPayload.Res> getPage(int page, int size, String sortBy, String sortDirection, String paramQuery);

    PermissionPayload.Res update(UUID id, PermissionPayload.UpdateCommandReq req);

    void delete(UUID id);
}
