package com.example.bulk_test_sample.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * S3 접근을 위한 클라이언트를 구성한다.
 */
@Configuration
@Profile("s3")
public class S3Config {

    /**
     * 기본 자격 증명 체인을 사용하는 S3 클라이언트를 생성한다.
     *
     * @return S3 클라이언트
     */
    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.AP_NORTHEAST_2)
                .build();
    }
}
