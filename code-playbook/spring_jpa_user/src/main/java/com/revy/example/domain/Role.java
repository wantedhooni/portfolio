package com.revy.example.domain;

import com.revy.example.domain.enums.RoleName;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;


@Entity
@Table(name = "role")
@Getter
public class Role extends BaseEntity<Long> {

    @Enumerated(EnumType.STRING)
    @Column(length = 60)
    private RoleName name;
}
