package com.revy.api.admin.server.api.administrator.role;

import com.revy.api.admin.server.api.administrator.role.payload.RolePayload;
import com.revy.api.admin.server.api.administrator.role.usecase.RoleUseCase;
import com.revy.common.web.api.response.ApiPageResponse;
import com.revy.api.admin.server.api.AbstractCrudApi;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/role")
@RequiredArgsConstructor
@Tag(name = "역할 관리 API", description = "역할 CRUD")
public class RoleApi extends
        AbstractCrudApi<RolePayload.CreateCommandReq, RolePayload.UpdateCommandReq, RolePayload.Res> {
    private final RoleUseCase roleUseCase;


    @Override
    protected ApiPageResponse<RolePayload.Res> getPage(int page, int size, String sortBy, String sortDirection,
                                                       String paramQuery) {
        return roleUseCase.getPage(page, size, sortBy, sortDirection, paramQuery);
    }

    @Override
    protected RolePayload.Res doCreate(RolePayload.CreateCommandReq req) {
        return roleUseCase.create(req);
    }

    @Override
    protected RolePayload.Res doGet(UUID id) {
        return roleUseCase.get(id);
    }

    @Override
    protected RolePayload.Res doUpdate(UUID id, RolePayload.UpdateCommandReq req) {
        return roleUseCase.update(id, req);
    }

    @Override
    protected void doDelete(UUID id) {
        roleUseCase.delete(id);
    }
}
