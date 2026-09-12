package com.revy.api.admin.server.api.administrator.permission;

import com.revy.api.admin.server.api.administrator.permission.payload.PermissionPayload;
import com.revy.api.admin.server.api.administrator.permission.usecase.PermissionUseCase;
import com.revy.common.web.api.response.ApiPageResponse;
import com.revy.api.admin.server.api.AbstractCrudApi;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/permission")
@RequiredArgsConstructor
@Tag(name = "권한 관리 API", description = "권한 CRUD")
public class PermissionApi extends
        AbstractCrudApi<PermissionPayload.CreateCommandReq, PermissionPayload.UpdateCommandReq, PermissionPayload.Res> {

    private final PermissionUseCase permissionUseCase;

    @Override
    protected ApiPageResponse<PermissionPayload.Res> getPage(int page, int size, String sortBy, String sortDirection,
                                                             String paramQuery) {
        return permissionUseCase.getPage(page, size, sortBy, sortDirection, paramQuery);
    }

    @Override
    protected PermissionPayload.Res doCreate(PermissionPayload.CreateCommandReq req) {
        return permissionUseCase.create(req);
    }

    @Override
    protected PermissionPayload.Res doGet(UUID id) {
        return permissionUseCase.get(id);
    }

    @Override
    protected PermissionPayload.Res doUpdate(UUID id, PermissionPayload.UpdateCommandReq req) {
        return permissionUseCase.update(id, req);
    }

    @Override
    protected void doDelete(UUID id) {
        permissionUseCase.delete(id);
    }

}
