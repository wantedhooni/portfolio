package com.example.bulk_test_sample.batch;

import com.example.bulk_test_sample.config.BulkExportProperties;
import com.example.bulk_test_sample.output.CsvOutput;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamWriter;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSetMetaData;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * CSV 스트리밍을 위한 ItemWriter.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CsvRowItemWriter implements ItemStreamWriter<String[]>, StepExecutionListener {

    private final CsvOutput csvOutput;
    private final BulkExportProperties exportProperties;
    private final CsvRowItemReader reader;

    private BufferedWriter writer;
    private boolean headerWritten = false;
    private String jobUuid;


    @PostConstruct
    public void init() {
        log.info("init bulk export job uuid: {}", jobUuid);
    }

    /**
     * 스텝 실행 정보를 받는다.
     *
     * @param stepExecution 스텝 실행 정보
     */
    @Override
    public void beforeStep(StepExecution stepExecution) {
        jobUuid = stepExecution.getJobParameters().getString("jobUuid");
    }

    /**
     * 스텝 종료 후 상태를 반환한다.
     *
     * @param stepExecution 스텝 실행 정보
     * @return 종료 상태
     */
    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        return stepExecution.getExitStatus();
    }

    /**
     * 출력 스트림을 준비한다.
     *
     * @param executionContext 실행 컨텍스트
     */
    @Override
    public void open(ExecutionContext executionContext) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String fileName = exportProperties.outputFilenamePrefix() + "-" + jobUuid + "-" + timestamp + ".csv";
        OutputStream outputStream = csvOutput.open(fileName);
        writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
    }

    /**
     * CSV 데이터를 기록한다.
     *
     * @param chunk 행 데이터 목록
     */
    @Override
    public void write(Chunk<? extends String[]> chunk) throws Exception {
        if (!headerWritten) {
            writeHeader(reader.getMetaData());
            headerWritten = true;
            log.info("write headerWritten... :{}", headerWritten);
        }

        log.info("chunk.size() :{}", chunk.size());
        for (String[] row : chunk) {
            writeRow(row);
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
     * 스트림을 닫는다.
     *
     */
    @Override
    public void close() throws ItemStreamException {
        if (writer != null) {
            try {
                writer.flush();
                writer.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }

    /**
     * CSV 헤더를 기록한다.
     *
     * @param metaData 컬럼 메타데이터
     * @throws Exception 기록 실패
     */
    private void writeHeader(ResultSetMetaData metaData) throws Exception {
        log.info("write header start...");
        int columnCount = metaData.getColumnCount();
        StringBuilder header = new StringBuilder();
        for (int i = 1; i <= columnCount; i++) {
            if (i > 1) {
                header.append(',');
            }
            header.append(escape(metaData.getColumnLabel(i)));
        }
        writer.write(header.toString());
        writer.newLine();
        log.info("write header end...");
    }

    /**
     * CSV 행을 기록한다.
     *
     * @param row 행 데이터
     * @throws Exception 기록 실패
     */
    private void writeRow(String[] row) throws Exception {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < row.length; i++) {
            if (i > 0) {
                line.append(',');
            }
            line.append(escape(row[i]));
        }
        writer.write(line.toString());
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
