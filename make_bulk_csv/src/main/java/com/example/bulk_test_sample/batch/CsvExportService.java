package com.example.bulk_test_sample.batch;

import com.example.bulk_test_sample.config.BulkExportProperties;
import com.example.bulk_test_sample.output.CsvOutput;
import com.example.bulk_test_sample.querydsl.UserExportQueryProvider;
import com.querydsl.sql.SQLBindings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

/**
 * JDBC 스트리밍으로 CSV를 내보내는 서비스.
 */
@Slf4j
@Service
public class CsvExportService {

    private final DataSource dataSource;
    private final UserExportQueryProvider queryProvider;
    private final CsvOutput csvOutput;
    private final BulkExportProperties exportProperties;

    /**
     * CSV 익스포트에 필요한 구성 요소를 주입받는다.
     *
     * @param dataSource 데이터 소스
     * @param queryProvider Querydsl SQL 생성기
     * @param csvOutput CSV 출력 전략
     * @param exportProperties 익스포트 설정
     */
    public CsvExportService(
            DataSource dataSource,
            UserExportQueryProvider queryProvider,
            CsvOutput csvOutput,
            BulkExportProperties exportProperties
    ) {
        this.dataSource = dataSource;
        this.queryProvider = queryProvider;
        this.csvOutput = csvOutput;
        this.exportProperties = exportProperties;
    }

    /**
     * CSV 익스포트를 수행한다.
     *
     * @param fileName 출력 파일명
     */
    public void export(String fileName) {
        log.info("export start");
        SQLBindings bindings = queryProvider.buildExportQuery();
        log.info("export query: {}", bindings.getSQL());

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(
                    bindings.getSQL(),
                    ResultSet.TYPE_FORWARD_ONLY,
                    ResultSet.CONCUR_READ_ONLY
            )) {
                statement.setFetchSize(exportProperties.fetchSize());
                for (int i = 1; i <= bindings.getNullFriendlyBindings().size(); i++) {
                    statement.setObject(i , bindings.getNullFriendlyBindings().get(i));
                }
                try (ResultSet resultSet = statement.executeQuery();
                     OutputStream outputStream = csvOutput.open(fileName);
                     BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8))) {
                    writeCsv(resultSet, writer);
                }
            }
        } catch (Exception ex) {
            throw new IllegalStateException("CSV 익스포트 실패", ex);
        }
    }

    private void writeCsv(ResultSet resultSet, BufferedWriter writer) throws Exception {
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();
        boolean headerWritten = false;
        while (resultSet.next()) {
            if (!headerWritten) {
                writeHeader(metaData, columnCount, writer);
                headerWritten = true;
            }
            writeRow(resultSet, columnCount, writer);
        }
        writer.flush();
        log.info("writeCsv end... ");
    }

    /**
     * CSV 헤더를 기록한다.
     *
     * @param metaData 컬럼 메타데이터
     * @param columnCount 컬럼 수
     * @param writer 출력 라이터
     * @throws Exception 기록 실패
     */
    private void writeHeader(ResultSetMetaData metaData, int columnCount, BufferedWriter writer) throws Exception {
        log.info("writeHeader start... ");
        StringBuilder header = new StringBuilder();
        for (int i = 1; i <= columnCount; i++) {
            if (i > 1) {
                header.append(',');
            }
            header.append(escape(metaData.getColumnLabel(i)));
        }
        writer.write(header.toString());
        writer.newLine();
    }

    /**
     * CSV 행을 기록한다.
     *
     * @param resultSet 결과 셋
     * @param columnCount 컬럼 수
     * @param writer 출력 라이터
     * @throws Exception 기록 실패
     */
    private void writeRow(ResultSet resultSet, int columnCount, BufferedWriter writer) throws Exception {
        StringBuilder row = new StringBuilder();
        for (int i = 1; i <= columnCount; i++) {
            if (i > 1) {
                row.append(',');
            }
            String value = resultSet.getString(i);
            row.append(escape(value));
        }
        writer.write(row.toString());
        writer.newLine();
    }

    /**
     * CSV 값 이스케이프를 수행한다.
     *
     * @param value 원본 값
     * @return 이스케이프된 값
     */
    private String escape(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\n") || escaped.contains("\r") || escaped.contains("\"")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }
}
