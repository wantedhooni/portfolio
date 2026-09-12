package com.example.bulk_test_sample.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 구성 속성 바인딩을 활성화한다.
 */
@Configuration
@EnableConfigurationProperties({BulkExportProperties.class, BulkOutputProperties.class})
public class PropertyConfig {
}
