package com.revy.domain.admin;

import com.revy.domain.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "admin_role")
@Getter
@NoArgsConstructor
public class AdminRole extends BaseEntity {

    // 예: ROLE_ADMIN, ROLE_USER
    @Column(nullable = false, length = 60)
    private String name;

    @Column(length = 200)
    private String description;

    @ManyToMany(mappedBy = "adminRoles", fetch = FetchType.LAZY)
    private Set<Admin> admins = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id", foreignKey = @ForeignKey(name = "fk_role_permissions_role")),
            inverseJoinColumns = @JoinColumn(name = "permission_id", foreignKey = @ForeignKey(name = "fk_role_permissions_permission")),
            uniqueConstraints = @UniqueConstraint(name = "uk_role_permissions", columnNames = {"role_id", "permission_id"})
    )
    private Set<AdminPermission> permissions = new HashSet<>();

    public AdminRole(String name) {
        this.name = name;
    }

    public static AdminRole create(String name, String description) {
        AdminRole adminRole = new AdminRole(name);
        adminRole.description = description;
        return adminRole;
    }

    public void changeName(String name) {
        this.name = name;
    }

    public void changeDescription(String description) {
        this.description = description;
    }

    // ---- convenience methods ----
    public void addPermission(AdminPermission permission) {
        this.permissions.add(permission);
        permission.getAdminRoles().add(this);
    }

    public void removePermission(AdminPermission permission) {
        this.permissions.remove(permission);
        permission.getAdminRoles().remove(this);
    }
}
