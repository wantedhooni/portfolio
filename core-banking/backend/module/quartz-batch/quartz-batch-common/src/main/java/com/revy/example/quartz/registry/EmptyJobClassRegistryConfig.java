package com.revy.example.quartz.registry;

import com.revy.example.quartz.exception.QuartzSchedulerException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 실행 노드 구현체가 없을 때 등록되는 기본 {@link JobClassRegistry}.
 *
 * <p>관리 노드(api-admin)는 Job 구현 클래스에 컴파일 의존이 없으므로
 * {@code createJob} 요청 시 명확한 오류 메시지를 반환합니다.
 *
 * <p>실행 노드(server-executor)에서는 {@code TypedJobRegistryConfig}가
 * 진짜 레지스트리 빈을 제공하므로 이 빈은 자동으로 비활성화됩니다.
 */
@Configuration
public class EmptyJobClassRegistryConfig {

    @Bean
    @ConditionalOnMissingBean(JobClassRegistry.class)
    public JobClassRegistry emptyJobClassRegistry() {
        return type -> {
            throw new QuartzSchedulerException(
                    "이 노드에는 Job 구현 클래스가 등록되어 있지 않습니다. " +
                    "실행 노드(server-executor)에서 Job을 등록하세요. type=" + type);
        };
    }
}
