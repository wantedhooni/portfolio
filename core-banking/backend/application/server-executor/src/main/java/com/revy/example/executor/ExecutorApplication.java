package com.revy.example.executor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Slf4j
@SpringBootApplication(scanBasePackages = "com.revy")
@ConfigurationPropertiesScan(basePackages = "com.revy")
@EntityScan(basePackages = "com.revy")
@EnableJpaRepositories(basePackages = "com.revy")
public class ExecutorApplication {


    public static void main(String[] args) {
        SpringApplication.run(ExecutorApplication.class, args);
    }

    @Autowired
    public Environment env;

    @EventListener
    public void on(ApplicationStartedEvent event) {
        log.info("ExecutorApplication Started");
    }
}
