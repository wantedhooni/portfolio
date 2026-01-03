package com.revy.springbatchquartz.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 커스텀 프로퍼티 바인딩을 활성화하는 설정 클래스.
 */
@Configuration
@EnableConfigurationProperties(AppProperties.class)
public class AppConfig {
}
