package com.revy.example.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name ="account" )
public class Account extends BaseEntity<Long> {
    private String accountNumber;
    private Long userId;
    private BigDecimal balance;
}

