package com.revy.example.doamin.bank;

import com.revy.example.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// domain/bank/Bank.java
@Entity
@Table(name = "bank")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Bank extends BaseEntity {

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, unique = true, length = 20)
    private String nationalCode;

    @Column(nullable = false, unique = true, length = 20)
    private String swiftCode;

    // ── 생성 팩토리 메서드 ──────────────────────────────
    public static Bank create(String name, String  nationalCode, String  swiftCode) {
        Bank bank = new Bank();
        bank.name = name;
        bank.nationalCode = nationalCode;
        bank.swiftCode = swiftCode;
        return bank;
    }
}
