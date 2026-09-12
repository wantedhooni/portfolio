package com.revy.example.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Table(name="user_detail")
@Entity
public class UserDetail extends BaseEntity<Long>{
    private String phone;
    @OneToOne
    @JoinColumn(name="user_id")
    private User user;
}
