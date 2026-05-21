package com.revy.example.admin.api.admin;

import com.revy.example.admin.api.admin.payload.AdminPayload;
import com.revy.example.admin.api.common.ApiConstants;
import com.revy.example.admin.api.admin.usecase.AdminUseCase;
import com.revy.example.admin.api.common.AbstractCrudApi;
import com.revy.example.core.common.ApiPageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@Slf4j
@RestController
@RequestMapping(ApiConstants.PREFIX_API_V1 + "/admin")
public class AdminController extends
        AbstractCrudApi<Long,
                AdminPayload.CreateRequest,
                AdminPayload.UpdateRequest,
                AdminPayload.SearchRequest,
                AdminPayload.ModelResponse> {

    private final AdminUseCase useCase ;

    public AdminController(AdminUseCase useCase) {
        this.useCase = useCase;
    }


    @Override
    protected ApiPageResponse<AdminPayload.ModelResponse> getPage(Pageable pageable,
                                                                  AdminPayload.SearchRequest searchRequest) {

        PageImpl<AdminPayload.ModelResponse> result = useCase.search(pageable, searchRequest);
        return ApiPageResponse.of(result.getContent(), result.getTotalElements(), result.getNumber(), result.getSize());
    }

    @Override
    protected AdminPayload.ModelResponse doCreate(AdminPayload.CreateRequest request) {
        return null;
    }

    @Override
    protected AdminPayload.ModelResponse doGet(Long id) {
        return useCase.getAdmin(id);
    }

    @Override
    protected AdminPayload.ModelResponse doUpdate(Long aLong, AdminPayload.UpdateRequest request) {
        return null;
    }

    @Override
    protected void doDelete(Long aLong) {

    }
}
