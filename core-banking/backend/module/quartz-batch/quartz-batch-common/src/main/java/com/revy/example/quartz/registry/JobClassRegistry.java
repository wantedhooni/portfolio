package com.revy.example.quartz.registry;

import com.revy.example.quartz.enums.JobType;
import org.quartz.Job;

/**
 * {@link JobType}을 실제 Quartz {@link Job} 구현 클래스로 해석하는 레지스트리.
 *
 * <p><b>구현 위치:</b>
 * <ul>
 *   <li>실행 노드(server-executor) — {@code TypedJob} 빈들을 수집해 동적으로 매핑 구축</li>
 *   <li>관리 노드(api-admin) — 기본 빈({@code EmptyJobClassRegistryConfig})이 명확한 오류 반환</li>
 * </ul>
 */
@FunctionalInterface
public interface JobClassRegistry {

    /**
     * @throws com.revy.example.quartz.exception.QuartzSchedulerException
     *         해당 유형의 Job이 현재 노드에 등록되어 있지 않은 경우
     */
    Class<? extends Job> resolve(JobType type);
}
