package com.example.bulk_test_sample.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * CSV 출력 대상별 설정 정보.
 *
 * @param localPath 로컬 파일 출력 경로
 * @param s3Bucket S3 버킷 이름
 * @param s3Prefix S3 키 프리픽스
 * @param s3PartSizeMb 멀티파트 업로드 파트 크기(MB)
 */
@ConfigurationProperties(prefix = "bulk.output")
public record BulkOutputProperties(
        String localPath,
        String s3Bucket,
        String s3Prefix,
        int s3PartSizeMb
) {
}
