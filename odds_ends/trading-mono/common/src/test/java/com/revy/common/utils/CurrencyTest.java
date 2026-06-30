package com.revy.common.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.Currency;


public class CurrencyTest {
    public static void main(String[] args) {
        Currency.getAvailableCurrencies()
                .stream()
                .sorted(Comparator.comparing(Currency::getCurrencyCode))
                .forEach(c -> {
                    String info = String.format("%s(\"%s\",\"%s\",%d),", c.getCurrencyCode(), c.getSymbol(), c.getNumericCodeAsString(), c.getDefaultFractionDigits());
                    System.out.println(info);
                });
    }
}
