package com.revy.example.admin.reader;

import aj.org.objectweb.asm.commons.Remapper;
import com.revy.example.doamin.admin.Admin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.Optional;

public interface AdminReader {
    Optional<Admin> findById(Long id);

    Optional<Admin> findByEmail( String email);
}
