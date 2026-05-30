package com.revy.example.quartz.listener;

import com.revy.example.quartz.domain.QuartzJobExecutionHistory;
import com.revy.example.quartz.domain.handler.QuartzJobHistoryHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.TriggerKey;
import org.quartz.listeners.JobListenerSupport;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuartzJobHistoryListener extends JobListenerSupport {

    private final QuartzJobHistoryHandler handler;


    @Override
    public String getName() {
        return "quartzJobHistoryListener";
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext context) {
        try {
            JobKey jobKey = context.getJobDetail()
                                   .getKey();
            TriggerKey triggerKey = context.getTrigger()
                                           .getKey();

            log.info("[QuartzListener] 실행 시작 — job={}, fireInstanceId={}", jobKey, context.getFireInstanceId());

            QuartzJobExecutionHistory history = QuartzJobExecutionHistory.running(context.getScheduler().getSchedulerName(),
                                                                                  context.getFireInstanceId(),
                                                                                  jobKey.getName(),
                                                                                  jobKey.getGroup(),
                                                                                  triggerKey.getName(),
                                                                                  triggerKey.getGroup(),
                                                                                  toLocalDateTime(context.getScheduledFireTime()),
                                                                                  toLocalDateTime(context.getFireTime()),
                                                                                  context.getRefireCount(),
                                                                                  context.isRecovering());

            handler.saveRunning(history);
        } catch (Exception e) {
            // Listener 실패가 Job 실행 자체에 영향을 주지 않도록 처리한다.
            log.error("[QuartzListener] RUNNING 이력 저장 실패 — fireInstanceId={}", context.getFireInstanceId(), e);
        }
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {
        try {
            log.warn("[QuartzListener] Job VETOED — fireInstanceId={}", context.getFireInstanceId());
            handler.markVetoed(context.getFireInstanceId(), LocalDateTime.now());
        } catch (Exception e) {
            log.error("[QuartzListener] VETOED 이력 저장 실패 — fireInstanceId={}", context.getFireInstanceId(), e);
        }
    }

    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        try {
            LocalDateTime endTime = LocalDateTime.now();
            long durationMs = context.getJobRunTime();

            if (jobException == null) {
                log.info("[QuartzListener] 실행 SUCCESS — job={}, durationMs={}",
                         context.getJobDetail()
                                .getKey(),
                         durationMs);
                handler.markSuccess(context.getFireInstanceId(), endTime, durationMs);
                return;
            }

            log.error("[QuartzListener] 실행 FAILED — job={}, durationMs={}, error={}",
                      context.getJobDetail()
                             .getKey(),
                      durationMs,
                      jobException.getMessage(),
                      jobException);
            handler.markFailed(context.getFireInstanceId(),
                               endTime,
                               durationMs,
                               jobException.getMessage(),
                               getStackTrace(jobException));
        } catch (Exception e) {
            log.error("[QuartzListener] 완료 이력 저장 실패 — fireInstanceId={}", context.getFireInstanceId(), e);
        }
    }

    private LocalDateTime toLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }

        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    private String getStackTrace(Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        throwable.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }
}