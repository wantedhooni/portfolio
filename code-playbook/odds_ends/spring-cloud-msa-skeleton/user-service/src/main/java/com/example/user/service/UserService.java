package com.example.user.service;

import org.springframework.stereotype.Service;

import com.example.common.error.ApiException;
import com.example.user.entity.UserEntity;
import com.example.user.repo.UserRepository;

@Service
public class UserService {

    private final UserRepository repo;

    public UserService(UserRepository repo) {
        this.repo = repo;
    }

    public UserEntity get(long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ApiException("USER_NOT_FOUND", "User not found: " + id));
    }
}
