package com.revy.example.quartz.config;

import com.revy.example.quartz.listener.QuartzJobHistoryListener;
import org.quartz.Scheduler;
import org.quartz.impl.matchers.EverythingMatcher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzListenerConfig {

    @Bean
    public CommandLineRunner registerQuartzJobHistoryListener(Scheduler scheduler, QuartzJobHistoryListener listener) {
        return args -> scheduler.getListenerManager()
            .addJobListener(listener, EverythingMatcher.allJobs());
    }
}