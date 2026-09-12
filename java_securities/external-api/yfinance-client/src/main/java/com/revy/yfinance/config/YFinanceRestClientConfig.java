package com.revy.yfinance.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(YFinanceClientProperties.class)
public class YFinanceRestClientConfig {
    /**
     * YFinance 프록시 서버로 요청을 보내는 RestClient를 생성한다.
     *
     * @param builder    RestClient 빌더
     * @param properties YFinance 프록시 설정
     * @return RestClient 인스턴스
     */
    @Bean
    public RestClient yFinanceRestClient(RestClient.Builder builder, YFinanceClientProperties properties) {
        return builder.baseUrl(properties.baseUrl()).build();
    }
}
