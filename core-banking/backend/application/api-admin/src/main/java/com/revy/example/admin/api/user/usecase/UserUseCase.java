package com.revy.example.admin.api.user.usecase;

import com.revy.example.admin.api.user.payload.UserPayload;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

public interface UserUseCase {
    PageImpl<UserPayload.ModelResponse> search(Pageable pageable, UserPayload.SearchRequest searchRequest);

    UserPayload.ModelResponse get(Long id);
}
