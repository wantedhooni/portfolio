package com.example.bulk_test_sample.batch;

import com.example.bulk_test_sample.config.BulkExportProperties;
import com.example.bulk_test_sample.querydsl.UserExportQueryProvider;
import com.querydsl.sql.SQLBindings;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

/**
 * JDBC 스트리밍 결과를 행 단위로 읽는 ItemReader.
 */
@Slf4j
@Component

public class CsvRowItemReader implements ItemStreamReader<String[]> {

    private final DataSource dataSource;
    private final UserExportQueryProvider queryProvider;
    private final BulkExportProperties exportProperties;

    private Connection connection;
    private PreparedStatement statement;
    private ResultSet resultSet;
    private ResultSetMetaData metaData;
    private int columnCount;

    /**
     * 리더에 필요한 구성 요소를 주입받는다.
     *
     * @param dataSource 데이터 소스
     * @param queryProvider Querydsl SQL 생성기
     * @param exportProperties 익스포트 설정
     */
    public CsvRowItemReader(
            DataSource dataSource,
            UserExportQueryProvider queryProvider,
            BulkExportProperties exportProperties
    ) {
        this.dataSource = dataSource;
        this.queryProvider = queryProvider;
        this.exportProperties = exportProperties;
    }

    @PostConstruct
    public void init() {
        log.info("init bulk export data source");
    }

    /**
     * 리더를 초기화한다.
     *
     * @param executionContext 실행 컨텍스트
     */
    @Override
    public void open(ExecutionContext executionContext) {
        log.info("open...");
        try {
            SQLBindings bindings = queryProvider.buildExportQuery();
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            statement = connection.prepareStatement(
                    bindings.getSQL(),
                    ResultSet.TYPE_FORWARD_ONLY,
                    ResultSet.CONCUR_READ_ONLY
            );
            statement.setFetchSize(exportProperties.fetchSize());
            for (int i = 1; i <= bindings.getNullFriendlyBindings().size(); i++) {
                statement.setObject(i , bindings.getNullFriendlyBindings().get(i));

            }
            resultSet = statement.executeQuery();
            metaData = resultSet.getMetaData();
            columnCount = metaData.getColumnCount();
        } catch (Exception ex) {
            throw new IllegalStateException("ItemReader 초기화 실패", ex);
        }
    }

    /**
     * 실행 컨텍스트 업데이트는 사용하지 않는다.
     *
     * @param executionContext 실행 컨텍스트
     */
    @Override
    public void update(ExecutionContext executionContext) {
    }

    /**
     * 다음 행을 읽는다.
     *
     * @return 행 데이터
     */
    @Override
    public String[] read() {
        try {
            if (resultSet != null && resultSet.next()) {
                String[] row = new String[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    row[i - 1] = resultSet.getString(i);
                }
                return row;
            }
            return null;
        } catch (Exception ex) {
            throw new IllegalStateException("ItemReader 읽기 실패", ex);
        }
    }

    /**
     * 리더 리소스를 정리한다.
     *
     */
    @Override
    public void close() {
        log.info("close start");
        try {
            if (resultSet != null) {
                resultSet.close();
            }
            if (statement != null) {
                statement.close();
            }
            if (connection != null) {
                connection.close();
            }
        } catch (Exception ex) {
            throw new IllegalStateException("ItemReader 종료 실패", ex);
        }
        log.info("close end");
    }

    /**
     * 컬럼 메타데이터를 반환한다.
     *
     * @return 메타데이터
     */
    public ResultSetMetaData getMetaData() {
        return metaData;
    }
}
