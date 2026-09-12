package com.revy.springbatchquartz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 스프링 배치와 쿼츠 통합 샘플 애플리케이션의 진입점 클래스.
 */
@SpringBootApplication
public class SpringBatchQuartzApplication {

    /**
     * 애플리케이션을 실행하는 메인 메서드.
     *
     * @param args 실행 인자
     */
    public static void main(String[] args) {
        SpringApplication.run(SpringBatchQuartzApplication.class, args);
    }
}
