package com.revy.example.scheduler.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(SchedulerProperties.class)
public class QuartzModeConfig {

    private final SchedulerProperties properties;
    private final Scheduler scheduler;

    /**
     * CONTROL 모드에서는 명시적으로 standby 상태로 진입.
     * Scheduler API 호출(triggerJob, pause, resume)은 JDBC JobStore 에 직접 반영되어
     * 워커 노드가 다음 폴링 주기에 인지합니다.
     */
    @EventListener(ContextRefreshedEvent.class)
    public void enterStandbyForControlMode()
        throws SchedulerException {
        if (properties.getMode() == SchedulerProperties.Mode.CONTROL) {
            scheduler.standby();
            log.info("[Quartz] CONTROL mode → scheduler in standby (cluster id={}).",
                     scheduler.getSchedulerInstanceId());
        } else {
            log.info("[Quartz] WORKER mode → scheduler started (cluster id={}).",
                     scheduler.getSchedulerInstanceId());
        }
    }
}
