package com.revy.securities.domain.user;

import com.revy.securities.domain.common.BaseEntity;
import com.revy.securities.domain.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 상세 정보를 저장하는 엔티티.
 */
@Entity
@Table(name = "user_detail")
@Getter
@NoArgsConstructor
public class UserDetail extends BaseEntity<Long> {

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String address;

    @Builder
    public UserDetail(User user, String name, String phone, String address) {
        this.user = user;
        this.name = name;
        this.phone = phone;
        this.address = address;
    }
}
