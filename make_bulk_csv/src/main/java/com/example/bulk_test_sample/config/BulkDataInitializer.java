package com.example.bulk_test_sample.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;

/**
 * 애플리케이션 시작 시 대용량 데이터를 생성한다.
 */
@Component
@Profile("local")
public class BulkDataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(BulkDataInitializer.class);

    private final JdbcTemplate jdbcTemplate;

    /**
     * 데이터 생성을 위한 JdbcTemplate을 주입받는다.
     *
     * @param jdbcTemplate JdbcTemplate
     */
    public BulkDataInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 애플리케이션 시작 후 데이터를 생성한다.
     *
     * @param args 실행 인자
     */
    @Override
    public void run(ApplicationArguments args) {
        Integer count = jdbcTemplate.queryForObject("SELECT id FROM users ORDER BY id ASC LIMIT 1", Integer.class);
        log.info("row count: {}", count);
        if (count != null && count > 0) {
            return;
        }
        int total = 1000000000;
        int batchSize = 10000;
        log.info("대용량 테스트 데이터 생성 시작: {}건", total);
        for (int i = 0; i < total; i += batchSize) {
            int finalI = i;
            log.info("current finalI: {}", finalI);
            jdbcTemplate.batchUpdate(
                    "INSERT INTO users (email, name, age, created_at) VALUES (?, ?, ?, ?)",
                    new org.springframework.jdbc.core.BatchPreparedStatementSetter() {
                        /**
                         * 배치 입력 값을 설정한다.
                         *
                         * @param ps PreparedStatement
                         * @param idx 인덱스
                         * @throws java.sql.SQLException 설정 실패
                         */
                        @Override
                        public void setValues(java.sql.PreparedStatement ps, int idx) throws java.sql.SQLException {
                            int sequence = finalI +idx + 1;
                            ps.setString(1, "user" + sequence + "@example.com");
                            ps.setString(2, "사용자" + sequence);
                            ps.setInt(3, 20 + (sequence % 30));
                            ps.setTimestamp(4, Timestamp.from(Instant.now()));
                        }

                        /**
                         * 배치 크기를 반환한다.
                         *
                         * @return 배치 크기
                         */
                        @Override
                        public int getBatchSize() {
                            return Math.min(batchSize, total - finalI);
                        }
                    }
            );
        }
        log.info("대용량 테스트 데이터 생성 완료");
    }
}
