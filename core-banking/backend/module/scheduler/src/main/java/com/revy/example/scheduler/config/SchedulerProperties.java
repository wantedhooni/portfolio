package com.revy.example.scheduler.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 스케줄러 클러스터 노드 설정.
 *
 * <p>{@code mode=CONTROL}일 때 Quartz Scheduler는 standby 상태로 유지되어
 * JDBC JobStore 에는 명령(triggerJob, pause, resume 등)을 발행하되,
 * 자신은 어떤 Job도 직접 실행하지 않습니다. 별도 워커 클러스터({@code mode=WORKER})만이
 * 실제 Job을 fire 합니다.</p>
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app.scheduler")
public class SchedulerProperties {

    /** 실행 모드 (CONTROL: api-admin / WORKER: 별도 클러스터) */
    private Mode mode = Mode.CONTROL;

    /** Quartz 인스턴스 이름 (클러스터 식별자). 동일 cluster 내 모든 노드가 공유. */
    private String schedulerInstanceName = "RevyCoreScheduler";

    /** 스레드 풀 크기 (CONTROL 모드에서는 1이면 충분) */
    private int threadPoolSize = 10;

    public enum Mode { CONTROL, WORKER }
}
