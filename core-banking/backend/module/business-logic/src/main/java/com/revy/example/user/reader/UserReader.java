package com.revy.example.user.reader;

import com.revy.example.domain.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface UserReader {
    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    Page<User> search(Pageable pageable, String name);
}
