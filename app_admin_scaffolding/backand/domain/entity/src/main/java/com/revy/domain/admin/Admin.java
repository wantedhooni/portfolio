package com.revy.domain.admin;

import com.revy.common.domain.enums.admin.AdminStatus;
import com.revy.domain.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "admin")
@Getter
@NoArgsConstructor
public class Admin extends BaseEntity {
    @Column(name="email", unique = true, nullable = false)
    private String email;

    @Column(name="password", nullable = false)
    private String password;

    @Column(name="status", nullable = false)
    @Enumerated(EnumType.STRING)
    private AdminStatus status;

    @Column(name="enabled")
    private boolean enabled;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "admin_roles",
            joinColumns = @JoinColumn(name = "admin_id", foreignKey = @ForeignKey(name = "fk_admin_roles_user")),
            inverseJoinColumns = @JoinColumn(name = "role_id", foreignKey = @ForeignKey(name = "fk_admin_roles_role")),
            uniqueConstraints = @UniqueConstraint(name = "uk_user_roles", columnNames = {"admin_id", "role_id"})
    )
    private Set<AdminRole> adminRoles = new HashSet<>();


    // ---- convenience methods ----
    public void addRole(AdminRole adminRole) {
        this.adminRoles.add(adminRole);
    }

    public void removeRole(AdminRole adminRole) {
        this.adminRoles.remove(adminRole);
    }

    public void changeEmail(String email) {
        this.email = email;
    }

    public void changePassword(String password) {
        this.password = password;
    }

    public void changeStatus(AdminStatus status) {
        this.status = status;
    }

    public void changeEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public static Admin create(String email, String password, AdminStatus status, boolean enabled) {
        Admin admin = new Admin();
        admin.email = email;
        admin.password = password;
        admin.status = status;
        admin.enabled = enabled;
        return admin;
    }
}
