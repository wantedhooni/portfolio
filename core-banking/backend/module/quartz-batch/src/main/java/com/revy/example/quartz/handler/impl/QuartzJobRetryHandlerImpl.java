package com.revy.example.quartz.handler.impl;

import com.revy.example.quartz.domain.QuartzJobExecutionHistory;
import com.revy.example.quartz.enums.QuartzJobExecutionStatus;
import com.revy.example.quartz.handler.QuartzJobRetryHandler;
import com.revy.example.quartz.reader.QuartzJobExecutionHistoryReader;
import lombok.RequiredArgsConstructor;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class QuartzJobRetryHandlerImpl implements QuartzJobRetryHandler {

    private final Scheduler scheduler;
    private final QuartzJobExecutionHistoryReader reader;

    @Transactional(readOnly = true)
    public void retry(Long historyId) throws Exception {
        QuartzJobExecutionHistory history = reader.findById(historyId)
            .orElseThrow(() -> new IllegalArgumentException(
                "실행 이력을 찾을 수 없습니다. id=" + historyId
            ));

        if (history.getStatus() != QuartzJobExecutionStatus.FAILED) {
            throw new IllegalArgumentException("실패 상태의 실행 이력만 재실행할 수 있습니다.");
        }

        JobKey jobKey = JobKey.jobKey(
            history.getJobName(),
            history.getJobGroup()
        );

        scheduler.triggerJob(jobKey);
    }
}
