package com.revy.securities.domain.user;


import com.revy.securities.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * 역할과 권한 매핑을 저장하는 엔티티.
 */
@Entity
@Table(name = "role")
@Getter
@Setter
@NoArgsConstructor
public class Role extends BaseEntity<Long> {

    @Column(nullable = false, unique = true)
    private String name;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "role_authoritiy",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "authority_id")
    )
    private Set<Authority> authorities = new HashSet<>();

    /**
     * 역할 이름을 설정한다.
     *
     * @param name 역할 이름
     */
    public Role(String name) {
        this.name = name;
    }

    /**
     * 역할에 권한을 추가한다.
     *
     * @param authority 권한 정보
     */
    public void addAuthority(Authority authority) {
        authorities.add(authority);
    }
}