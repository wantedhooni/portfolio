package com.revy.api_server.application.init;

import com.revy.securities.domain.exchange.ExRate;
import com.revy.securities.domain.exchange.ExchangeConfig;
import com.revy.securities.domain.exchange.service.ExchangeReteService;
import com.revy.common.enums.Currency;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExchangeDataInitializer implements CommandLineRunner {

    private final ExchangeReteService exchangeReteService;


    // 환율 조회 API가 필요한데 지금은 없으니 저장소 하나 만들어 놓자.

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (exchangeReteService.getExchangeRateConfigCount() == 0) {
            var sourceList = List.of(Currency.USD, Currency.KRW);
            var destList = List.of(Currency.USD, Currency.KRW, Currency.JPY);

            for (Currency source : sourceList) {
                for (Currency dest : destList) {
                    if (source != dest) {
                        ExchangeConfig config = ExchangeConfig.createNewConfig(source, dest);
                        config.enable();
                        exchangeReteService.save(config);
                    }
                }
            }
        }

        if (exchangeReteService.getExchangeRateCount() == 0) {
            LocalDateTime now = LocalDateTime.now();
            List<ExRate> rateList = exchangeReteService.findAllConfig()
                                                       .stream()
                                                       .filter(ExchangeConfig::isEnabled)
                                                       .map(
                    config -> {
                        return ExRate.createNewExchangeRate(
                                config.getSource(), config.getDest(), now,
                                dummyRateApi.getRate(config.getSource(), config.getDest())
                        );
                    }
            ).toList();
            exchangeReteService.saveExchangeRate(rateList);
        }
    }

    class dummyRateApi {
        static final Map<String, BigDecimal> rateMap = Map.of(
                "USD-KRW", BigDecimal.valueOf(1300.50),
                "USD-JPY", BigDecimal.valueOf(145.30),
                "KRW-USD", BigDecimal.valueOf(0.00077),
                "KRW-JPY", BigDecimal.valueOf(0.11)
        );

        public static BigDecimal getRate(Currency source, Currency dest) {
            return rateMap.get(source + "-" + dest);
        }
    }
}
