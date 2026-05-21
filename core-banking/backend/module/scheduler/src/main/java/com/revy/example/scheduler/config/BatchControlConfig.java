package com.revy.example.scheduler.config;

import org.springframework.context.annotation.Configuration;

/**
 * Spring Batch 6.x 는 {@code JobOperator}, {@code JobExplorer}, {@code JobRegistry} 를
 * Spring Boot autoconfiguration 이 자동 등록한다. 별도 빈 설정은 필요하지 않다.
 *
 * <p>워커 노드는 본 모듈을 의존성에 추가하고 자체 {@code Job} 빈을 정의하면 된다.
 * api-admin(CONTROL)은 Job bean 이 없어도 read / stop / abandon API 를 정상 사용 가능.</p>
 */
@Configuration
public class BatchControlConfig {
}
