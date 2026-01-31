package com.revy.api_server.domain.exchange;


import com.revy.common.enums.Currency;
import com.revy.common.utils.UuidUtils;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "exchange_rate_history",
        indexes = {
                @Index(name = "IDX_EXCHANGE_RATE_HISTORY_ID_SOURCE_DEST", columnList = "id, source, dest"),
                @Index(name = "IDX_EXCHANGE_RATE_HISTORY_SOURCE_DEST_RATETIME", columnList = "source, dest,rateTime")
        }

)
@Getter
@ToString(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExchangeRateHistory extends BaseExchangeRate {

        public static ExchangeRateHistory createExchangeRateHistory(BaseExchangeRate baseExchangeRate) {
                ExchangeRateHistory exchangeRate = new ExchangeRateHistory();
                exchangeRate.id = baseExchangeRate.getId();
                exchangeRate.source = baseExchangeRate.source;
                exchangeRate.dest =  baseExchangeRate.dest;
                exchangeRate.rateTime =  baseExchangeRate.rateTime;
                exchangeRate.rate =  baseExchangeRate.getRate();
                return exchangeRate;
        }

        /**
         * ExchangeRate에 저장된 ID와 맞추기 위해 자동 생성을 방지한다.
         */
        @Override
        protected void onPrePersist() {
                if (id == null) {
                        throw new IllegalArgumentException("Exchange rate history ID is required");
                }
        }
}
