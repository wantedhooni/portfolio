package com.revy.yfinance.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * YFinance 프록시 호출에 필요한 설정 값을 담는 프로퍼티 클래스.
 *
 * @param baseUrl 프록시 서비스 기본 URL
 */
@ConfigurationProperties(prefix = "yfinance")
public record YFinanceClientProperties(
        String baseUrl) {
}
