package com.revy.example.quartz.listener;

import com.revy.example.quartz.domain.QuartzJobExecutionHistory;
import com.revy.example.quartz.handler.QuartzJobHistoryHandler;
import lombok.RequiredArgsConstructor;
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
            JobKey jobKey = context.getJobDetail().getKey();
            TriggerKey triggerKey = context.getTrigger().getKey();

            QuartzJobExecutionHistory history = QuartzJobExecutionHistory.running(
                context.getScheduler().getSchedulerName(),
                context.getFireInstanceId(),
                jobKey.getName(),
                jobKey.getGroup(),
                triggerKey.getName(),
                triggerKey.getGroup(),
                toLocalDateTime(context.getScheduledFireTime()),
                toLocalDateTime(context.getFireTime()),
                context.getRefireCount(),
                context.isRecovering()
            );

            handler.saveRunning(history);
        } catch (Exception e) {
            // Listener 실패가 Job 실행 자체에 영향을 주지 않도록 처리한다.
            // log.error("Quartz Job 실행 이력 RUNNING 저장 실패", e);
        }
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {
        try {
            handler.markVetoed(
                context.getFireInstanceId(),
                LocalDateTime.now()
            );
        } catch (Exception e) {
            // log.error("Quartz Job VETOED 이력 저장 실패", e);
        }
    }

    @Override
    public void jobWasExecuted(
        JobExecutionContext context,
        JobExecutionException jobException
    ) {
        try {
            LocalDateTime endTime = LocalDateTime.now();
            long durationMs = context.getJobRunTime();

            if (jobException == null) {
                handler.markSuccess(
                    context.getFireInstanceId(),
                    endTime,
                    durationMs
                );
                return;
            }

            handler.markFailed(
                context.getFireInstanceId(),
                endTime,
                durationMs,
                jobException.getMessage(),
                getStackTrace(jobException)
            );
        } catch (Exception e) {
            // log.error("Quartz Job 완료 이력 저장 실패", e);
        }
    }

    private LocalDateTime toLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }

        return LocalDateTime.ofInstant(
            date.toInstant(),
            ZoneId.systemDefault()
        );
    }

    private String getStackTrace(Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        throwable.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }
}