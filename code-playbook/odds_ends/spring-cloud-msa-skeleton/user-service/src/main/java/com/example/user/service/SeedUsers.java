package com.example.user.service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.user.entity.UserEntity;
import com.example.user.repo.UserRepository;

@Configuration
public class SeedUsers {

    @Bean
    CommandLineRunner seed(UserRepository repo) {
        return args -> {
            if (repo.count() == 0) {
                repo.save(new UserEntity("user", "Demo User"));
                repo.save(new UserEntity("admin", "Demo Admin"));
            }
        };
    }
}
