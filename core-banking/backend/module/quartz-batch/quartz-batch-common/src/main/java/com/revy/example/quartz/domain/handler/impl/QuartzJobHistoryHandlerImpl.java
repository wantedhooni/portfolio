package com.revy.example.quartz.domain.handler.impl;

import com.revy.example.quartz.domain.QuartzJobExecutionHistory;
import com.revy.example.quartz.domain.handler.QuartzJobHistoryHandler;
import com.revy.example.quartz.domain.reader.QuartzJobExecutionHistoryReader;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


@Component
@RequiredArgsConstructor
public class QuartzJobHistoryHandlerImpl implements QuartzJobHistoryHandler {

    private final EntityManager entityManager;
    private final QuartzJobExecutionHistoryReader reader;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void saveRunning(QuartzJobExecutionHistory history) {
        entityManager.persist(history);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void markSuccess(String fireInstanceId, LocalDateTime endTime, long durationMs) {
        QuartzJobExecutionHistory history = reader.findByFireInstanceId(fireInstanceId)
            .orElseThrow(() -> new IllegalStateException("Quartz 실행 이력을 찾을 수 없습니다. fireInstanceId=" + fireInstanceId));

        history.success(endTime, durationMs);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void markFailed(String fireInstanceId, LocalDateTime endTime, long durationMs, String errorMessage,
                           String errorStackTrace) {
        QuartzJobExecutionHistory history = reader.findByFireInstanceId(fireInstanceId)
            .orElseThrow(() -> new IllegalStateException("Quartz 실행 이력을 찾을 수 없습니다. fireInstanceId=" + fireInstanceId));

        history.failed(endTime, durationMs, errorMessage, errorStackTrace);
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void markVetoed(String fireInstanceId, LocalDateTime endTime) {
        QuartzJobExecutionHistory history = reader.findByFireInstanceId(fireInstanceId)
            .orElseThrow(() -> new IllegalStateException("Quartz 실행 이력을 찾을 수 없습니다. fireInstanceId=" + fireInstanceId));

        history.vetoed(endTime);
    }
}
