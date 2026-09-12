package com.revy.example.service;



import com.revy.example.controller.UserCreateRequest;
import com.revy.example.service.mapper.UserMapper;
import com.revy.example.controller.UserResponse;
import com.revy.example.entity.User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class UserService {

    private final UserMapper userMapper;
    private final AtomicLong sequence = new AtomicLong(1);

    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public UserResponse create(UserCreateRequest request) {
        User user = userMapper.toEntity(request);

        user.setId(sequence.getAndIncrement());
        user.setCreatedAt(LocalDateTime.now());

        return userMapper.toResponse(user);
    }
}