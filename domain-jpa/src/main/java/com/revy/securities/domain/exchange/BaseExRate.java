package com.revy.securities.domain.exchange;

import com.revy.common.enums.Currency;
import com.revy.securities.domain.common.BaseUUIDEntity;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@MappedSuperclass
@ToString
public class BaseExRate extends BaseUUIDEntity {

    @Enumerated(EnumType.STRING)
    @Column(name="source", nullable = false)
    protected Currency source;

    @Enumerated(EnumType.STRING)
    @Column(name="dest", nullable = false)
    protected Currency dest;

    @Column(name="rate_time", nullable = false)
    protected LocalDateTime rateTime;

    @Column(name = "rate", nullable = false, precision = 19, scale = 8)
    protected BigDecimal rate;
}
