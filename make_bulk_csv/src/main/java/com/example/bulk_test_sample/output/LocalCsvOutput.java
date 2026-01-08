package com.example.bulk_test_sample.output;

import com.example.bulk_test_sample.config.BulkOutputProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;

/**
 * 로컬 파일 시스템에 CSV를 출력한다.
 */
@Component
@Profile("local")
public class LocalCsvOutput implements CsvOutput {

    private final BulkOutputProperties properties;

    /**
     * 로컬 출력 경로 설정을 주입받는다.
     *
     * @param properties 출력 설정
     */
    public LocalCsvOutput(BulkOutputProperties properties) {
        this.properties = properties;
    }

    /**
     * 지정된 경로에 CSV 출력 스트림을 생성한다.
     *
     * @param fileName 파일 이름
     * @return 출력 스트림
     */
    @Override
    public OutputStream open(String fileName) {
        try {
            File directory = new File(properties.localPath());
            if (!directory.exists()) {
                Files.createDirectories(directory.toPath());
            }
            File target = new File(directory, fileName);
            return new FileOutputStream(target);
        } catch (Exception ex) {
            throw new IllegalStateException("로컬 CSV 출력 스트림 생성 실패", ex);
        }
    }
}
