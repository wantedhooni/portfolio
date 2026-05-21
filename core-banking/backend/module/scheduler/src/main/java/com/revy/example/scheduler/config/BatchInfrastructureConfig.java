package com.revy.example.scheduler.config;

import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.support.MapJobRegistry;
import org.springframework.batch.core.converter.DefaultJobParametersConverter;
import org.springframework.batch.core.converter.JobParametersConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Batch 6.x 는 {@code JobOperator}, {@code JobRepository}, {@code JobRegistry} 를
 * Spring Boot autoconfiguration 이 자동 등록한다.
 *
 * <p>{@link JobParametersConverter} 는 {@code start(String, Properties)} 폐기로 인해
 * 워커 측 launch 로직에서 직접 사용해야 하므로 기본 구현을 등록한다.</p>
 *
 * <p>워커 노드는 본 모듈을 의존성에 추가하고 자체 {@code Job} 빈을 정의하면 된다.
 * api-admin(CONTROL)은 Job bean 이 없어도 read / stop / abandon API 를 정상 사용 가능.</p>
 */
@Configuration
public class BatchInfrastructureConfig {

    @Bean
    public JobParametersConverter jobParametersConverter() {
        return new DefaultJobParametersConverter();
    }

    @Bean
    public JobRegistry jobRegistry() {
        return new MapJobRegistry();
    }
}
