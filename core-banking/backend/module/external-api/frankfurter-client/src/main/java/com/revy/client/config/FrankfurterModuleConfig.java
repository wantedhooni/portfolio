package com.revy.client.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(FrankfurterProperties.class)
public class FrankfurterModuleConfig {
}