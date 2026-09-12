package com.revy.api.admin.server.api.user;

import com.revy.api.admin.server.api.user.payload.UserPayload;
import com.revy.api.admin.server.api.user.usecase.UserUseCase;
import com.revy.common.web.api.response.ApiPageResponse;
import com.revy.api.admin.server.api.AbstractCrudApi;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "User 관리 API", description = "User CRUD")
public class UserApi extends AbstractCrudApi<UserPayload.CreateReq, UserPayload.UpdateReq, UserPayload.Res> {
    private final UserUseCase userUseCase;

    @Override
    protected ApiPageResponse<UserPayload.Res> getPage(int page, int size, String sortBy, String sortDirection,
                                                       String paramQuery) {
        log.debug("page: {}, size: {}, sortBy:{}, sortDirection:{}, sortByparamQuery:{}", page, size, sortBy,
                  sortDirection, paramQuery);
        return userUseCase.getPage(page, size, sortBy, sortDirection, paramQuery);
    }

    @Override
    protected UserPayload.Res doCreate(UserPayload.CreateReq req) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected UserPayload.Res doGet(UUID id) {
        return userUseCase.get(id);
    }

    @Override
    protected UserPayload.Res doUpdate(UUID id, UserPayload.UpdateReq req) {
        return userUseCase.update(id, req);
    }

    @Override
    protected void doDelete(UUID id) {
        userUseCase.delete(id);
    }
}
