package com.example.bulk_test_sample.output;

import com.example.bulk_test_sample.config.BulkOutputProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.OutputStream;

/**
 * S3 멀티파트 업로드로 CSV를 출력한다.
 */
@Component
@Profile("s3")
public class S3CsvOutput implements CsvOutput {

    private final S3Client s3Client;
    private final BulkOutputProperties properties;

    /**
     * S3 출력에 필요한 구성 요소를 주입받는다.
     *
     * @param s3Client S3 클라이언트
     * @param properties 출력 설정
     */
    public S3CsvOutput(S3Client s3Client, BulkOutputProperties properties) {
        this.s3Client = s3Client;
        this.properties = properties;
    }

    /**
     * S3 멀티파트 업로드를 위한 스트림을 연다.
     *
     * @param fileName 파일 이름
     * @return 출력 스트림
     */
    @Override
    public OutputStream open(String fileName) {
        String key = properties.s3Prefix() + "/" + fileName;
        return new S3MultipartOutputStream(
                s3Client,
                properties.s3Bucket(),
                key,
                properties.s3PartSizeMb() * 1024L * 1024L
        );
    }
}
