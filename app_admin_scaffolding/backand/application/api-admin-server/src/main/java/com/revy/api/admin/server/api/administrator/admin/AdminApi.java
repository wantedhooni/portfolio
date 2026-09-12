package com.revy.api.admin.server.api.administrator.admin;

import com.revy.api.admin.server.api.administrator.admin.payload.AdminPayload;
import com.revy.api.admin.server.api.administrator.admin.usecase.AdminUseCase;
import com.revy.common.web.api.response.ApiPageResponse;
import com.revy.api.admin.server.api.AbstractCrudApi;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;


@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "사용자 관리 API", description = "사용자 CRUD")
public class AdminApi extends AbstractCrudApi<AdminPayload.CreateReq, AdminPayload.UpdateCommandReq, AdminPayload.Res> {
    private final AdminUseCase adminUseCase;


    @Override
    protected ApiPageResponse<AdminPayload.Res> getPage(int page, int size, String sortBy, String sortDirection,
                                                        String paramQuery) {
        log.info("page: {}, size: {}, sort by: {}, sort direction: {}, paramQuery:{}", page, size, sortBy,
                 sortDirection, paramQuery);
        return adminUseCase.getPage(page, size, sortBy, sortDirection, paramQuery);
    }

    @Override
    protected AdminPayload.Res doCreate(AdminPayload.CreateReq req) {
        return adminUseCase.create(req);
    }

    @Override
    protected AdminPayload.Res doGet(UUID id) {
        return adminUseCase.get(id);
    }

    @Override
    protected AdminPayload.Res doUpdate(UUID id, AdminPayload.UpdateCommandReq req) {
        return adminUseCase.update(id, req);
    }

    @Override
    protected void doDelete(UUID id) {
        adminUseCase.delete(id);
    }
}
