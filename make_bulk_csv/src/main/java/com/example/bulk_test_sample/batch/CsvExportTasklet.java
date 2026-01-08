package com.example.bulk_test_sample.batch;

import com.example.bulk_test_sample.config.BulkExportProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Tasklet 방식으로 CSV 익스포트를 수행한다.
 */
@Slf4j
@Component
public class CsvExportTasklet implements Tasklet {

    private final CsvExportService exportService;
    private final BulkExportProperties exportProperties;

    /**
     * Tasklet에 필요한 서비스와 설정을 주입받는다.
     *
     * @param exportService CSV 익스포트 서비스
     * @param exportProperties 익스포트 설정
     */
    public CsvExportTasklet(CsvExportService exportService, BulkExportProperties exportProperties) {
        this.exportService = exportService;
        this.exportProperties = exportProperties;
    }

    /**
     * CSV 익스포트를 실행한다.
     *
     * @param contribution 스텝 기여 정보
     * @param chunkContext 청크 컨텍스트
     * @return 완료 상태
     */
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        log.info("execute start");
        String jobUuid = (String) chunkContext.getStepContext().getJobParameters().get("jobUuid");
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String fileName = exportProperties.outputFilenamePrefix() + "-" + jobUuid + "-" + timestamp + ".csv";
        exportService.export(fileName);
        log.info("execute end");
        return RepeatStatus.FINISHED;
    }
}
