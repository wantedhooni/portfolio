package com.revy.example.quartz.handler;

import com.revy.example.quartz.domain.QuartzJobExecutionHistory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

public interface QuartzJobHistoryHandler {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void saveRunning(QuartzJobExecutionHistory history);

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void markSuccess(String fireInstanceId, LocalDateTime endTime, long durationMs);

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void markFailed(String fireInstanceId, LocalDateTime endTime, long durationMs, String errorMessage,
                    String errorStackTrace);

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void markVetoed(String fireInstanceId, LocalDateTime now);
}
