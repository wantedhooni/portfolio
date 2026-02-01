package com.revy.securities.domain.user;



import com.revy.securities.domain.common.BaseEntity;
import com.revy.securities.domain.user.Role;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.util.Assert;

import java.util.HashSet;
import java.util.Set;

/**
 * 사용자 기본 정보를 저장하는 엔티티.
 */
@Entity
@Table(name = "user")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class User extends BaseEntity<Long> {

    @Column(nullable = false, unique = true)
    private String email;

    @ToString.Exclude
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.ACTIVE;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private UserDetail detail;

    public void addRole(Role role) {
        roles.add(role);
    }

    public void withdraw(){
        Assert.isTrue(this.status == UserStatus.ACTIVE, "회원 상태는 ACTIVE 상태여야 합니다.");
        this.status =  UserStatus.WITHDRAWN;
    }
}
