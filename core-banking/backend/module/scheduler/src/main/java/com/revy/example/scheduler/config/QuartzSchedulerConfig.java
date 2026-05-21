package com.revy.example.scheduler.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.quartz.autoconfigure.SchedulerFactoryBeanCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * Quartz Scheduler 공통 설정.
 *
 * <p>JDBC JobStore + Cluster 모드. CONTROL 노드(=api-admin)는 standby 상태로 유지되어
 * 명령은 발행하되 Job을 직접 fire하지 않습니다. WORKER 노드만 start() 합니다.</p>
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(SchedulerProperties.class)
public class QuartzSchedulerConfig {

    private final SchedulerProperties properties;

    @Bean
    public SchedulerFactoryBeanCustomizer schedulerFactoryBeanCustomizer() {
        return factory -> {
            Properties props = new Properties();
            props.put("org.quartz.scheduler.instanceName", properties.getSchedulerInstanceName());
            props.put("org.quartz.scheduler.instanceId", "AUTO");
            // 클러스터 모드 활성화
            props.put("org.quartz.jobStore.isClustered", "true");
            props.put("org.quartz.jobStore.clusterCheckinInterval", "15000");
            props.put("org.quartz.jobStore.driverDelegateClass",
                    "org.quartz.impl.jdbcjobstore.PostgreSQLDelegate");
            props.put("org.quartz.jobStore.tablePrefix", "QRTZ_");
            props.put("org.quartz.jobStore.useProperties", "false");
            props.put("org.quartz.threadPool.threadCount",
                    String.valueOf(properties.getThreadPoolSize()));

            factory.setQuartzProperties(props);
            factory.setOverwriteExistingJobs(false);
            factory.setWaitForJobsToCompleteOnShutdown(true);

            // CONTROL 노드는 autostart 하지 않음 - standby 모드로 머무름
            factory.setAutoStartup(properties.getMode() == SchedulerProperties.Mode.WORKER);
        };
    }


}
