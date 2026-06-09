package com.revy.example.quartz.batch.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Spring Batch 대시보드 상단 집계 카드용 통계.
 */
@Getter
@Builder
public class BatchSummaryDto {
    private final long totalExecutions;
    private final long completedCount;
    private final long failedCount;
    private final long runningCount;
    /** 잡 별 최근 실행 현황. */
    private final List<JobStat> jobs;

    @Getter
    @Builder
    public static class JobStat {
        private final String jobName;
        private final long totalCount;
        private final long failedCount;
        private final String lastStatus;
        private final java.time.LocalDateTime lastExecutionTime;
    }
}
