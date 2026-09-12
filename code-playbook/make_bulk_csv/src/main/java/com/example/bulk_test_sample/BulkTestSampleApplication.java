package com.example.bulk_test_sample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 대용량 CSV 익스포트를 위한 샘플 애플리케이션 엔트리 포인트.
 */
@SpringBootApplication
public class BulkTestSampleApplication {

    /**
     * 애플리케이션을 실행한다.
     *
     * @param args 실행 인자
     */
    public static void main(String[] args) {
        SpringApplication.run(BulkTestSampleApplication.class, args);
    }
}
