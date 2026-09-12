package com.revy.executor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;

@Slf4j
@EntityScan(basePackages = "com.revy")
@ConfigurationPropertiesScan
@SpringBootApplication(scanBasePackages={"com.revy"})
class ExecutorApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExecutorApplication.class, args);
    }

    @EventListener
    public void on(ApplicationStartedEvent event) {
        log.info("Application started");
    }

    @EventListener
    public void on(ContextClosedEvent event) {
        log.info("Application stopped");
    }
}
