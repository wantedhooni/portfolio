package com.revy.example.domain.admin;

import com.revy.example.domain.common.BaseEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.EnumSet;
import java.util.Set;

@Entity
@Table(name = "admin_role",
       uniqueConstraints = @UniqueConstraint(name = "uq_admin_role_name", columnNames = "name"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class AdminRole extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(length = 255)
    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "admin_role_permission", joinColumns = @JoinColumn(name = "role_id"))
    @Column(name = "permission", length = 50)
    private Set<AdminPermission> permissions = EnumSet.noneOf(AdminPermission.class);

    public static AdminRole create(String name, String description, Set<AdminPermission> permissions) {
        AdminRole role = new AdminRole();
        role.name = name;
        role.description = description;
        role.permissions = permissions.isEmpty()
                ? EnumSet.noneOf(AdminPermission.class)
                : EnumSet.copyOf(permissions);
        return role;
    }

    public void update(String name, String description, Set<AdminPermission> permissions) {
        this.name = name;
        this.description = description;
        this.permissions = permissions.isEmpty()
                ? EnumSet.noneOf(AdminPermission.class)
                : EnumSet.copyOf(permissions);
    }
}
