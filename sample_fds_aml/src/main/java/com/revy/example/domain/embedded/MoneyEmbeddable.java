package com.revy.example.domain.embedded;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MoneyEmbeddable {

    private BigDecimal amount;

    private String currency;

    private MoneyEmbeddable(BigDecimal amount, String currency) {
        this.amount = amount;
        this.currency = currency;
    }

    public static MoneyEmbeddable of(BigDecimal amount, String currency) {
        return new MoneyEmbeddable(amount, currency);
    }
}
