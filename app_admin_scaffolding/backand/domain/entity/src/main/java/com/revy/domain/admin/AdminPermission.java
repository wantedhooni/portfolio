package com.revy.domain.admin;

import com.revy.domain.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "admin_permission")
@Getter
@NoArgsConstructor
public class AdminPermission extends BaseEntity {
    @Column(nullable = false, length = 80)
    private String code;

    @Column(length = 200)
    private String description;

    @ManyToMany(mappedBy = "permissions", fetch = FetchType.LAZY)
    private Set<AdminRole> adminRoles = new HashSet<>();

    public AdminPermission(String code) {
        this.code = code;
    }

    public static AdminPermission create(String code, String description) {
        AdminPermission permission = new AdminPermission(code);
        permission.description = description;
        return permission;
    }

    public void changeCode(String code) {
        this.code = code;
    }

    public void changeDescription(String description) {
        this.description = description;
    }
}
