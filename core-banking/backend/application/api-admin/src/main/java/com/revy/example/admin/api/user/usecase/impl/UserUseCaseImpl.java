package com.revy.example.admin.api.user.usecase.impl;

import com.revy.example.admin.api.user.payload.UserPayload;
import com.revy.example.admin.api.user.usecase.UserUseCase;
import com.revy.example.domain.user.User;
import com.revy.example.user.reader.UserCommand;
import com.revy.example.user.reader.UserReader;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserUseCaseImpl implements UserUseCase {
    private final UserCommand userCommand;
    private final UserReader userReader;


    @Override
    public PageImpl<UserPayload.ModelResponse> search(Pageable pageable, UserPayload.SearchRequest searchRequest) {
        var result = userReader.search(pageable, searchRequest.name());
        return new PageImpl<>(map(result.getContent()), pageable, result.getTotalElements());
    }

    @Override
    public UserPayload.ModelResponse get(Long id) {
        return toModelResponse(userReader.findById(id).get());
    }

    private UserPayload.ModelResponse toModelResponse(User user) {
        return new UserPayload.ModelResponse(user.getId(), user.getEmail(), user.getPassword());
    }

    private List<UserPayload.ModelResponse> map(List<User> users) {
        if(users == null || users.isEmpty()){
            return List.of();
        }
        return users.stream().map(this::map).toList();
    }
    private UserPayload.ModelResponse map(User user){
        return new UserPayload.ModelResponse(user.getId(), user.getEmail(), user.getName());
    }

}
