package com.revy.example.admin.api.user.usecase.impl;

import com.revy.example.admin.api.user.payload.UserPayload;
import com.revy.example.admin.api.user.usecase.UserUseCase;
import com.revy.example.core.error.BusinessException;
import com.revy.example.core.error.ErrorCode;
import com.revy.example.user.reader.UserReader;
import com.revy.example.user.reader.dto.UserResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserUseCaseImpl implements UserUseCase {

    private final UserReader userReader;

    @Override
    public PageImpl<UserPayload.ModelResponse> search(Pageable pageable, UserPayload.SearchRequest searchRequest) {
        Page<UserResult> result = userReader.search(pageable, searchRequest.name());
        return new PageImpl<>(map(result.getContent()), pageable, result.getTotalElements());
    }

    @Override
    public UserPayload.ModelResponse get(Long id) {
        UserResult user = userReader.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return map(user);
    }

    private List<UserPayload.ModelResponse> map(List<UserResult> users) {
        if (users == null || users.isEmpty()) {
            return List.of();
        }
        return users.stream().map(this::map).toList();
    }

    private UserPayload.ModelResponse map(UserResult user) {
        return new UserPayload.ModelResponse(user.id(), user.email(), user.name());
    }
}
