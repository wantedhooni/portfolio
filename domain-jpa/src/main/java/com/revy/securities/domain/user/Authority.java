package com.revy.securities.domain.user;

import com.revy.securities.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "authoritiy")
@Getter
@Setter
@NoArgsConstructor
public class Authority extends BaseEntity<Long> {

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String description;

    /**
     * 권한 정보를 생성한다.
     *
     * @param name 권한 이름
     * @param description 권한 설명
     */
    public Authority(String name, String description) {
        this.name = name;
        this.description = description;
    }
}