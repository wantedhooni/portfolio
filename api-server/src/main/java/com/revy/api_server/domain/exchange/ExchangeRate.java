package com.revy.api_server.domain.exchange;

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
@Table(name = "exchange_rate",
        uniqueConstraints = {
                @UniqueConstraint(name = "UK_EXCHANGE_RATE_SOURCE_DEST", columnNames = {"source", "dest"})
        },
        indexes = {
                @Index(name = "IDX_EXCHANGE_RATE_SOURCE_DEST", columnList = "source, dest")
        }
)
@Getter
@ToString(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExchangeRate extends BaseExchangeRate {

    public static ExchangeRate createNewExchangeRate(Currency source, Currency dest, LocalDateTime rateTime, BigDecimal rate) {
        ExchangeRate exchangeRate = new ExchangeRate();
        exchangeRate.id = UuidUtils.getTimeOrderedEpochUuidV7();
        exchangeRate.source = source;
        exchangeRate.dest = dest;
        exchangeRate.rateTime = rateTime;
        exchangeRate.rate = rate;
        return exchangeRate;
    }

}