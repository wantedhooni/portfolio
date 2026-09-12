package com.revy.api.admin.server.api.user.usecase;

import com.revy.api.admin.server.api.user.payload.UserPayload;
import com.revy.common.web.api.response.ApiPageResponse;

import java.util.UUID;

public interface UserUseCase {
    ApiPageResponse<UserPayload.Res> getPage(int page, int size, String sortBy, String sortDirection, String paramQuery);

    UserPayload.Res create(UserPayload.CreateReq req);

    UserPayload.Res get(UUID id);

    UserPayload.Res update(UUID id, UserPayload.UpdateReq req);

    void delete(UUID id);
}
