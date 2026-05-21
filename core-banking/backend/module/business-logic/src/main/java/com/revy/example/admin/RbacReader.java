package com.revy.example.admin;

import com.revy.example.admin.dto.AdminRoleResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface RbacReader {

    Optional<AdminRoleResult> findRoleById(Long id);

    Optional<AdminRoleResult> findRoleByName(String name);

    boolean existsByName(String name);

    Page<AdminRoleResult> searchRoles(Pageable pageable, String name);
}
