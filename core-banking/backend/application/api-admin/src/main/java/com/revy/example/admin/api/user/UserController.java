package com.revy.example.admin.api.user;

import com.revy.example.admin.api.common.AbstractCrudApi;
import com.revy.example.admin.api.common.ApiConstants;
import com.revy.example.admin.api.user.payload.UserPayload;
import com.revy.example.admin.api.user.usecase.UserUseCase;
import com.revy.example.core.common.ApiPageResponse;
import com.revy.example.core.common.ApiResponse;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(ApiConstants.PREFIX_API_V1 + "/user")
public class UserController extends
        AbstractCrudApi<Long, UserPayload.CreateRequest, UserPayload.UpdateRequest, UserPayload.SearchRequest, UserPayload.ModelResponse> {

    private final UserUseCase useCase;

    @Override
    protected ApiPageResponse<UserPayload.ModelResponse> getPage(Pageable pageable,
                                                                 UserPayload.SearchRequest searchRequest) {
        log.debug("Fetching user page with pageable: {} and search request: {}", pageable, searchRequest);
        PageImpl<UserPayload.ModelResponse> result = useCase.search(pageable, searchRequest);
        return ApiPageResponse.of(result.getContent(), result.getTotalElements(), result.getNumber(), result.getSize());
    }


    @Override
    protected UserPayload.ModelResponse doUpdate(Long id, UserPayload.UpdateRequest request) {
        return null;
    }


    @Hidden
    @Override
    public ResponseEntity<ApiResponse<UserPayload.ModelResponse>> create(UserPayload.CreateRequest request) {
        throw new UnsupportedOperationException("User creation is not supported");
    }

    @Override
    protected UserPayload.ModelResponse doCreate(UserPayload.CreateRequest request) {
        throw new UnsupportedOperationException("User creation is not supported");
    }

    @Override
    protected UserPayload.ModelResponse doGet(Long id) {
        return useCase.get(id);
    }

    @Hidden
    @Override
    public ResponseEntity<ApiResponse<Void>> delete(Long aLong) {
        throw new UnsupportedOperationException("User creation is not supported");
    }

    @Override
    protected void doDelete(Long aLong) {
        throw new UnsupportedOperationException("User creation is not supported");
    }
}
