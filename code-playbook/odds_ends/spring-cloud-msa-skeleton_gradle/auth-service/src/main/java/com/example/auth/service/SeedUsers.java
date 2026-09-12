package com.example.auth.service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.auth.entity.AuthUserEntity;
import com.example.auth.repo.AuthUserRepository;

@Configuration
public class SeedUsers {

    @Bean
    CommandLineRunner seed(AuthUserRepository repo, PasswordEncoder encoder) {
        return args -> {
            if (repo.count() == 0) {
                repo.save(new AuthUserEntity("user", encoder.encode("pass"), "USER"));
                repo.save(new AuthUserEntity("admin", encoder.encode("pass"), "ADMIN"));
            }
        };
    }
}
