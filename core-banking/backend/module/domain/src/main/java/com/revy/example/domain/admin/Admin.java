package com.revy.example.domain.admin;

import com.revy.example.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "admin")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter(AccessLevel.PUBLIC)
public class Admin extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 30)
    private String role;

    public static Admin create(String email, String encode, String name) {
        Admin admin = new Admin();
        admin.email = email;
        admin.password = encode;
        admin.name = name;
        admin.role = "ROLE_ADMIN";
        return admin;
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void changeRole(String role) {
        this.role = role;
    }
}
