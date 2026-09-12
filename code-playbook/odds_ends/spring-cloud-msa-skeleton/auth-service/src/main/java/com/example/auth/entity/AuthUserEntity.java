package com.example.auth.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "auth_users")
public class AuthUserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    // comma separated roles: USER,ADMIN
    @Column(nullable = false)
    private String roles;

    protected AuthUserEntity() {}

    public AuthUserEntity(String username, String passwordHash, String roles) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.roles = roles;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getRoles() { return roles; }
}
