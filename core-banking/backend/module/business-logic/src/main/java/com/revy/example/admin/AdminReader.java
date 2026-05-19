package com.revy.example.admin;

import com.revy.example.domain.admin.Admin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface AdminReader {

    Optional<Admin> findById(Long id);

    Optional<Admin> findByEmail(String email);

    Page<Admin> search(Pageable pageable, String name);
}
