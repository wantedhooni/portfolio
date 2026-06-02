package com.revy.example.entity;

import java.time.LocalDateTime;

public class User {

    private Long id;
    private String email;
    private String name;
    private String password;
    private LocalDateTime createdAt;

    public User() {
    }

    public User(Long id, String email, String name, String password, LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.password = password;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}