package com.revy.securities.domain.exchange;

import com.revy.securities.domain.common.BaseUUIDEntity;
import com.revy.common.enums.Currency;
import com.revy.common.utils.UuidUtils;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 환율 구간 설정
 */
@Entity
@Table(name = "exchange_config",
        uniqueConstraints = {
                @UniqueConstraint(name = "UK_EXCHANGE_CONFIG_SOURCE_DEST", columnNames = {"source", "dest"})
        },
        indexes = {
                @Index(name = "IDX_EXCHANGE_CONFIG_SOURCE_DEST", columnList = "source, dest")
        }
)
@Getter
@ToString(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExchangeConfig extends BaseUUIDEntity {
    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false)
    private Currency source;

    @Enumerated(EnumType.STRING)
    @Column(name = "dest", nullable = false)
    private Currency dest;

    @Column(name = "enabled", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean enabled;

    public void disable() {
        this.enabled = false;
    }

    public void enable() {
        this.enabled = true;
    }

    public static ExchangeConfig createNewConfig(Currency source, Currency dest) {
        ExchangeConfig config = new ExchangeConfig();
        config.id = UuidUtils.getTimeOrderedEpochUuidV7();
        config.source = source;
        config.dest = dest;
        return config;
    }

}
