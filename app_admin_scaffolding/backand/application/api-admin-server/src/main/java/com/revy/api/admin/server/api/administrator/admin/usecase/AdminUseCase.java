package com.revy.api.admin.server.api.administrator.admin.usecase;

import com.revy.api.admin.server.api.administrator.admin.payload.AdminPayload;
import com.revy.common.web.api.response.ApiPageResponse;

import java.util.UUID;

public interface AdminUseCase {
    AdminPayload.Res create(AdminPayload.CreateReq req);

    AdminPayload.Res get(UUID adminId);

    ApiPageResponse<AdminPayload.Res> getPage(int page, int size, String sortBy, String sortDirection, String paramQuery);

    AdminPayload.Res update(UUID adminId, AdminPayload.UpdateCommandReq req);

    AdminPayload.DeleteRes delete(UUID adminId);
}
