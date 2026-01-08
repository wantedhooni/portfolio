package com.example.bulk_test_sample.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 대용량 CSV 익스포트 작업에 필요한 설정 정보.
 *
 * @param fetchSize JDBC 스트리밍 페치 사이즈
 * @param outputFilenamePrefix 출력 파일 이름 접두어
 */
@ConfigurationProperties(prefix = "bulk.export")
public record BulkExportProperties(
        int fetchSize,
        String outputFilenamePrefix
) {
}
