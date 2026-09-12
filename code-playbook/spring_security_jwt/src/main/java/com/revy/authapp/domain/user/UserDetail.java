package com.revy.authapp.domain.user;

import com.revy.authapp.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
