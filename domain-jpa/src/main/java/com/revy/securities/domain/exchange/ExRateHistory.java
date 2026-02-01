package com.revy.securities.domain.exchange;


import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "exrate_history",
        indexes = {
                @Index(name = "IDX_EXRATE_HISTORY_ID_SOURCE_DEST", columnList = "id, source, dest"),
                @Index(name = "IDX_EXRATE_HISTORY_SOURCE_DEST_RATETIME", columnList = "source, dest,rateTime")
        }

)
@Getter
@ToString(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExRateHistory extends BaseExRate {

        public static ExRateHistory createExchangeRateHistory(BaseExRate baseExRate) {
                ExRateHistory exchangeRate = new ExRateHistory();
                exchangeRate.id = baseExRate.getId();
                exchangeRate.source = baseExRate.source;
                exchangeRate.dest =  baseExRate.dest;
                exchangeRate.rateTime =  baseExRate.rateTime;
                exchangeRate.rate =  baseExRate.getRate();
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
