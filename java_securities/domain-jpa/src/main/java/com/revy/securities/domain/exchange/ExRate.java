package com.revy.securities.domain.exchange;

import com.revy.securities.domain.exchange.BaseExRate;
import com.revy.common.enums.Currency;
import com.revy.common.utils.UuidUtils;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "exrate",
        uniqueConstraints = {
                @UniqueConstraint(name = "UK_EXRATE_SOURCE_DEST", columnNames = {"source", "dest"})
        },
        indexes = {
                @Index(name = "IDX_EXRATE_SOURCE_DEST", columnList = "source, dest")
        }
)
@Getter
@ToString(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExRate extends BaseExRate {

    public static ExRate createNewExchangeRate(Currency source, Currency dest, LocalDateTime rateTime, BigDecimal rate) {
        ExRate exRate = new ExRate();
        exRate.id = UuidUtils.getTimeOrderedEpochUuidV7();
        exRate.source = source;
        exRate.dest = dest;
        exRate.rateTime = rateTime;
        exRate.rate = rate;
        return exRate;
    }

}