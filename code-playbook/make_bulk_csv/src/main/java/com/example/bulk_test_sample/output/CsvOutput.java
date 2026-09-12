package com.example.bulk_test_sample.output;

import java.io.OutputStream;

/**
 * CSV 출력 대상 추상화.
 */
public interface CsvOutput {

    /**
     * CSV 출력 스트림을 연다.
     *
     * @param fileName 파일 이름
     * @return 출력 스트림
     */
    OutputStream open(String fileName);
}
