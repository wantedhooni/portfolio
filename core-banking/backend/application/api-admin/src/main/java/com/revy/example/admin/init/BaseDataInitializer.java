package com.revy.example.admin.init;

import com.revy.example.common.enums.Currency;
import com.revy.example.fx.command.FxCommand;
import com.revy.example.fx.command.dto.RegisterCurrencyCommand;
import com.revy.example.fx.reader.FxReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BaseDataInitializer implements ApplicationRunner {

    private final FxCommand fxCommand;
    private final FxReader fxReader;

    @Override
    public void run(ApplicationArguments args) {
        // Base Currency 등록
        List<Currency> baseCurrencies = List.of(Currency.USD, Currency.KRW, Currency.CNY, Currency.JPY, Currency.VND,
                                                Currency.THB);
        for (Currency baseCurrency : baseCurrencies) {
            if (fxReader.existsCurrencyByCode(baseCurrency.name())) {
                log.info("Currencies already exists for currency {}", baseCurrency.name());
                continue;
            }
            RegisterCurrencyCommand registerCurrencyCommand = new RegisterCurrencyCommand(baseCurrency.name(),
                                                                                          baseCurrency.getName(),
                                                                                          baseCurrency.getSymbol(),
                                                                                          baseCurrency.getDigits());
            fxCommand.registerCurrency(registerCurrencyCommand);
        }
    }
}

