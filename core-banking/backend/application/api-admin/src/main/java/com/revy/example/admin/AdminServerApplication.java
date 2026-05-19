package com.revy.example.admin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.revy")
@ConfigurationPropertiesScan(basePackages = "com.revy")
@EntityScan(basePackages = "com.revy")
@EnableJpaRepositories(basePackages = "com.revy")
class AdminServerApplication {

    private static final Logger log = LoggerFactory.getLogger(AdminServerApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(AdminServerApplication.class, args);
    }

    @Autowired
    public Environment env;

    @EventListener
    public void on(ApplicationStartedEvent event) {
        log.info("Admin Server Application Started");
        log.info("APP_PROFILE = {}", System.getenv("APP_PROFILE"));
        log.info("SPRING_PROFILES_ACTIVE = {}", env.getActiveProfiles().toString());


    }
}
