package com.revy.example.quartz.config;

import com.revy.example.quartz.TypedJob;
import com.revy.example.quartz.enums.JobType;
import com.revy.example.quartz.exception.QuartzSchedulerException;
import com.revy.example.quartz.registry.JobClassRegistry;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 실행 노드(server-executor)에서 활성화되는 {@link JobClassRegistry}를 등록합니다.
 *
 * <p>Spring 컨텍스트에 존재하는 모든 {@link TypedJob} 빈을 수집하여
 * {@code (JobType → Class)} 매핑을 자동 구축합니다.
 * 새 Job 추가 시 별도의 등록 작업 없이 {@code @Component + TypedJob} 구현만으로 인식됩니다.
 *
 * <p>중복 {@link JobType}이 발견되면 즉시 실패합니다 (구성 오류 조기 발견).
 *
 * <p>{@code EmptyJobClassRegistryConfig}의 fallback 빈은 {@code @ConditionalOnMissingBean}이므로
 * 이 빈이 등록되면 자동으로 비활성화됩니다.
 */
@Slf4j
@Configuration
public class TypedJobRegistryConfig {

    @Bean
    public JobClassRegistry typedJobClassRegistry(List<TypedJob> typedJobs) {
        Map<JobType, Class<? extends Job>> mapping = new EnumMap<>(JobType.class);

        for (TypedJob job : typedJobs) {
            JobType type = job.getType();
            Class<? extends Job> jobClass = job.getClass();

            Class<? extends Job> previous = mapping.put(type, jobClass);
            if (previous != null && !previous.equals(jobClass)) {
                throw new QuartzSchedulerException(
                        "JobType [" + type + "]에 대해 복수의 Job 구현이 등록되었습니다: "
                                + previous.getName() + ", " + jobClass.getName());
            }
        }

        log.info("[TypedJobRegistry] Job 매핑 구축 완료 size={} entries={}",
                mapping.size(), mapping.keySet());

        return type -> {
            Class<? extends Job> resolved = mapping.get(type);
            if (resolved == null) {
                throw new QuartzSchedulerException(
                        "등록된 Job 구현이 없습니다. type=" + type);
            }
            return resolved;
        };
    }
}
