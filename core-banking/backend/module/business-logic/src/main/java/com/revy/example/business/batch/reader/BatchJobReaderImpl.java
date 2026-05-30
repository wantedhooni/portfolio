package com.revy.example.business.batch.reader;

import com.revy.example.business.batch.dto.BatchJobExecutionDetailDto;
import com.revy.example.business.batch.dto.BatchJobExecutionDto;
import com.revy.example.business.batch.dto.BatchStepExecutionDto;
import com.revy.example.business.batch.dto.BatchSummaryDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.job.parameters.JobParameter;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Spring Batch 메타데이터를 {@link JobRepository} API 로 조회한다.
 *
 * <p>배치 잡은 별도 워커(server-executor)에서 실행되며 동일 DB 에 메타데이터를
 * 적재한다. 본 Reader 는 그 결과를 읽기 전용으로 노출한다.</p>
 *
 * <p>Spring Batch 6.0 에서 {@code JobExplorer} 가 제거되고 조회 메서드가
 * {@link JobRepository} 로 통합되었으므로 JdbcTemplate 직접 조회 대신
 * 표준 API 만 사용한다.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BatchJobReaderImpl implements BatchJobReader {

    /**
     * 잡 인스턴스 페이지네이션 시 한 번에 가져올 최대 건수(메타데이터 조회 상한).
     */
    private static final int MAX_INSTANCES_PER_JOB = 1000;

    private final JobRepository jobRepository;

    @Override
    public BatchSummaryDto getSummary() {
        List<JobExecution> all = loadExecutions(null);

        long completed = all.stream().filter(e -> e.getStatus() == BatchStatus.COMPLETED).count();
        long failed = all.stream().filter(e -> e.getStatus() == BatchStatus.FAILED).count();
        long running = all.stream().filter(e -> e.getStatus().isRunning()).count();

        // 잡 이름별 그룹핑 → 최근 실행 시각 DESC 정렬
        Map<String, List<JobExecution>> byName = new LinkedHashMap<>();
        for (JobExecution e : all) {
            byName.computeIfAbsent(jobName(e), k -> new ArrayList<>()).add(e);
        }

        List<BatchSummaryDto.JobStat> jobs = new ArrayList<>();
        for (Map.Entry<String, List<JobExecution>> entry : byName.entrySet()) {
            List<JobExecution> execs = entry.getValue();
            execs.sort(Comparator.comparing(JobExecution::getId).reversed());
            JobExecution latest = execs.get(0);
            long jobFailed = execs.stream().filter(e -> e.getStatus() == BatchStatus.FAILED).count();
            jobs.add(BatchSummaryDto.JobStat.builder().jobName(entry.getKey()).totalCount(execs.size())
                                            .failedCount(jobFailed)
                                            .lastStatus(latest.getStatus() == null ? null : latest.getStatus().name())
                                            .lastExecutionTime(latest.getCreateTime()).build());
        }
        jobs.sort(Comparator.comparing(BatchSummaryDto.JobStat::getLastExecutionTime,
                                       Comparator.nullsLast(Comparator.reverseOrder())));

        return BatchSummaryDto.builder().totalExecutions(all.size()).completedCount(completed).failedCount(failed)
                              .runningCount(running).jobs(jobs).build();
    }

    @Override
    public List<String> getJobNames() {
        return jobRepository.getJobNames();
    }

    @Override
    public List<BatchJobExecutionDto> getExecutions(String jobName, String status, int limit) {
        int safeLimit = limit <= 0 ? 50 : Math.min(limit, 500);

        return loadExecutions(jobName).stream()
                                      .filter(e -> status == null || status.isBlank() || (e.getStatus() != null && e.getStatus()
                                                                                                                    .name()
                                                                                                                    .equals(
                                                                                                                        status)))
                                      .sorted(Comparator.comparing(JobExecution::getId).reversed()).limit(safeLimit)
                                      .map(this::toExecutionDto).toList();
    }

    @Override
    public BatchJobExecutionDetailDto getExecutionDetail(Long jobExecutionId) {
        JobExecution execution = jobRepository.getJobExecution(jobExecutionId);
        if (execution == null) {
            return null;
        }

        Map<String, String> params = new LinkedHashMap<>();
        execution.getJobParameters().forEach(param -> params.put(param.name(), stringValue(param)));

        List<BatchStepExecutionDto> steps = execution.getStepExecutions().stream()
                                                     .sorted(Comparator.comparing(StepExecution::getId))
                                                     .map(this::toStepDto).toList();

        ExitStatus exit = execution.getExitStatus();
        return BatchJobExecutionDetailDto.builder().execution(toExecutionDto(execution))
                                         .exitMessage(exit == null ? null : exit.getExitDescription())
                                         .jobParameters(params).steps(steps).build();
    }

    // ── 내부 헬퍼 ────────────────────────────────────────────────

    /**
     * 잡 이름(nullable)에 해당하는 모든 JobExecution 을 메타데이터에서 로드.
     * jobName 이 null 이면 등록된 모든 잡을 대상으로 한다.
     */
    private List<JobExecution> loadExecutions(String jobName) {
        List<String> names = (jobName != null && !jobName.isBlank()) ? List.of(jobName) : jobRepository.getJobNames();
        List<JobExecution> result = new ArrayList<>();
        for (String name : names) {
            List<JobInstance> instances = jobRepository.getJobInstances(name, 0, MAX_INSTANCES_PER_JOB);
            for (JobInstance instance : instances) {
                result.addAll(jobRepository.getJobExecutions(instance));
            }
        }
        return result;
    }

    private BatchJobExecutionDto toExecutionDto(JobExecution e) {
        ExitStatus exit = e.getExitStatus();
        return BatchJobExecutionDto.builder().jobExecutionId(e.getId())
                                   .jobInstanceId(e.getJobInstance() == null ? null : e.getJobInstance()
                                                                                       .getInstanceId())
                                   .jobName(jobName(e)).status(e.getStatus() == null ? null : e.getStatus().name())
                                   .exitCode(exit == null ? null : exit.getExitCode()).createTime(e.getCreateTime())
                                   .startTime(e.getStartTime()).endTime(e.getEndTime())
                                   .durationMs(duration(e.getStartTime(), e.getEndTime())).build();
    }

    private BatchStepExecutionDto toStepDto(StepExecution s) {
        ExitStatus exit = s.getExitStatus();
        return BatchStepExecutionDto.builder().stepExecutionId(s.getId()).stepName(s.getStepName())
                                    .status(s.getStatus() == null ? null : s.getStatus().name())
                                    .exitCode(exit == null ? null : exit.getExitCode()).startTime(s.getStartTime())
                                    .endTime(s.getEndTime()).durationMs(duration(s.getStartTime(), s.getEndTime()))
                                    .readCount(s.getReadCount()).writeCount(s.getWriteCount())
                                    .commitCount(s.getCommitCount()).rollbackCount(s.getRollbackCount())
                                    .filterCount(s.getFilterCount()).readSkipCount(s.getReadSkipCount())
                                    .writeSkipCount(s.getWriteSkipCount()).processSkipCount(s.getProcessSkipCount())
                                    .exitMessage(exit == null ? null : exit.getExitDescription()).build();
    }

    private static String jobName(JobExecution e) {
        return e.getJobInstance() == null ? null : e.getJobInstance().getJobName();
    }

    private static String stringValue(JobParameter<?> param) {
        Object v = param == null ? null : param.value();
        return v == null ? null : String.valueOf(v);
    }

    private static Long duration(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return null;
        }
        return ChronoUnit.MILLIS.between(start, end);
    }
}
