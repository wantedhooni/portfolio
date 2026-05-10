package com.revy.example.saas;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.web.server.context.WebServerInitializedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Component;

@Slf4j
@SpringBootApplication(scanBasePackages = "com.revy")
@ConfigurationPropertiesScan(basePackages = "com.revy")
@EntityScan(basePackages = "com.revy")
@EnableJpaRepositories(basePackages = "com.revy")
class SaasServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SaasServerApplication.class, args);
    }


}