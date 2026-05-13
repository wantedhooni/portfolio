package com.revy.example.admin.api.admin;

import com.revy.example.admin.api.common.ApiConstants;
import com.revy.example.admin.api.admin.payload.AdminCreatePayload;
import com.revy.example.admin.api.admin.payload.AdminSearchPayload;
import com.revy.example.admin.api.admin.payload.AdminUpdatePayload;
import com.revy.example.admin.api.admin.usecase.AdminUseCase;
import com.revy.example.admin.api.common.AbstractCrudApi;
import com.revy.example.core.common.ApiPageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequestMapping(ApiConstants.PREFIX_API_V1 + "/admin")
public class AdminController extends
        AbstractCrudApi<Long, AdminCreatePayload.Request, AdminUpdatePayload.Request, AdminSearchPayload.Request, Void> {

    private final AdminUseCase useCase ;

    public AdminController(AdminUseCase useCase) {
        this.useCase = useCase;
    }

    @Override
    protected ApiPageResponse<Void> getPage(Pageable pageable, AdminSearchPayload.Request searchRequest) {
        return null;
    }

    @Override
    protected Void doCreate(AdminCreatePayload.Request request) {
        return null;
    }

    @Override
    protected Void doGet(Long aLong) {
        return null;
    }

    @Override
    protected Void doUpdate(Long aLong, AdminUpdatePayload.Request request) {
        return null;
    }

    @Override
    protected void doDelete(Long aLong) {

    }
}
