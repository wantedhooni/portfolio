package com.revy.example.user.reader;

import com.revy.example.doamin.user.User;

import java.util.Optional;

public interface UserReader {
    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);
}
