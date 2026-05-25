package com.revy.example.quartz.handler.impl;

import com.revy.example.quartz.TriggerFactory;
import com.revy.example.quartz.domain.QuartzJobExecutionHistory;
import com.revy.example.quartz.dto.QuartzJobResult;
import com.revy.example.quartz.dto.QuartzJobUpsertCommand;
import com.revy.example.quartz.dto.QuartzRunningJobResult;
import com.revy.example.quartz.enums.QuartzJobExecutionStatus;
import com.revy.example.quartz.exception.QuartzSchedulerException;
import com.revy.example.quartz.handler.QuartzJobHandler;
import com.revy.example.quartz.reader.QuartzJobExecutionHistoryReader;
import lombok.RequiredArgsConstructor;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class QuartzJobHandlerImpl implements QuartzJobHandler {

    private final Scheduler                      scheduler;
    private final QuartzJobExecutionHistoryReader reader;

    @Override
    public void createJob(QuartzJobUpsertCommand command) {
        try {
            JobKey jobKey = JobKey.jobKey(command.jobName(), command.jobGroup());

            if (scheduler.checkExists(jobKey)) {
                throw new IllegalArgumentException("이미 존재하는 Job입니다. jobKey=" + jobKey);
            }

            Class<? extends Job> jobClass = command.jobType().toJobClass();
            JobDataMap jobDataMap = buildJobDataMap(command);

            JobDetail jobDetail = JobBuilder.newJob(jobClass)
                    .withIdentity(jobKey)
                    .withDescription(command.description())
                    .usingJobData(jobDataMap)
                    .storeDurably(false)
                    .build();

            Trigger trigger = TriggerFactory.createTrigger(command.jobName(), command.jobGroup(), command);
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            throw new QuartzSchedulerException(e.getMessage(), e);
        }
    }

    @Override
    public void updateJob(String jobGroup, String jobName, QuartzJobUpsertCommand command) {
        try {
            JobKey jobKey = JobKey.jobKey(jobName, jobGroup);

            if (!scheduler.checkExists(jobKey)) {
                throw new IllegalArgumentException("존재하지 않는 Job입니다. jobKey=" + jobKey);
            }

            scheduler.deleteJob(jobKey);

            QuartzJobUpsertCommand normalizedCommand = new QuartzJobUpsertCommand(
                    jobName, jobGroup, command.jobType(),
                    command.scheduleType(), command.cronExpression(),
                    command.repeatIntervalMs(), command.repeatCount(),
                    command.startAt(), command.description(), command.jobData()
            );

            createJob(normalizedCommand);
        } catch (SchedulerException e) {
            throw new QuartzSchedulerException(e.getMessage(), e);
        }
    }

    @Override
    public void updateScheduleOnly(String jobGroup, String jobName, QuartzJobUpsertCommand command) {
        try {
            JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
            validateJobExists(jobKey);

            Trigger oldTrigger = scheduler.getTriggersOfJob(jobKey)
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Job에 연결된 Trigger가 없습니다."));

            Trigger newTrigger = TriggerFactory.createTrigger(jobName, jobGroup, command);
            scheduler.rescheduleJob(oldTrigger.getKey(), newTrigger);
        } catch (SchedulerException e) {
            throw new QuartzSchedulerException(e.getMessage(), e);
        }
    }

    @Override
    public List<QuartzJobResult> findAllJobs() {
        try {
            List<QuartzJobResult> result = new ArrayList<>();
            for (String groupName : scheduler.getJobGroupNames()) {
                for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
                    JobDetail jobDetail = scheduler.getJobDetail(jobKey);
                    for (Trigger trigger : scheduler.getTriggersOfJob(jobKey)) {
                        Trigger.TriggerState state = scheduler.getTriggerState(trigger.getKey());
                        result.add(new QuartzJobResult(
                                jobKey.getName(), jobKey.getGroup(),
                                trigger.getKey().getName(), trigger.getKey().getGroup(),
                                state.name(),
                                trigger instanceof CronTrigger ? "CRON" : trigger.getClass().getSimpleName(),
                                jobDetail.getDescription(),
                                toInstant(trigger.getPreviousFireTime()),
                                toInstant(trigger.getNextFireTime())
                        ));
                    }
                }
            }
            return result;
        } catch (SchedulerException e) {
            throw new QuartzSchedulerException(e.getMessage(), e);
        }
    }

    @Override
    public List<QuartzRunningJobResult> findRunningJobs() {
        try {
            return scheduler.getCurrentlyExecutingJobs()
                    .stream()
                    .map(context -> {
                        JobKey jobKey = context.getJobDetail().getKey();
                        TriggerKey triggerKey = context.getTrigger().getKey();
                        long runningTimeMs = context.getFireTime() == null
                                ? 0L
                                : System.currentTimeMillis() - context.getFireTime().getTime();
                        return new QuartzRunningJobResult(
                                jobKey.getName(), jobKey.getGroup(),
                                triggerKey.getName(), triggerKey.getGroup(),
                                toInstant(context.getFireTime()),
                                toInstant(context.getScheduledFireTime()),
                                runningTimeMs
                        );
                    })
                    .toList();
        } catch (SchedulerException e) {
            throw new QuartzSchedulerException(e.getMessage(), e);
        }
    }

    @Override
    public void pauseJob(String jobGroup, String jobName) {
        try {
            JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
            validateJobExists(jobKey);
            scheduler.pauseJob(jobKey);
        } catch (SchedulerException e) {
            throw new QuartzSchedulerException(e.getMessage(), e);
        }
    }

    @Override
    public void resumeJob(String jobGroup, String jobName) {
        try {
            JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
            validateJobExists(jobKey);
            scheduler.resumeJob(jobKey);
        } catch (SchedulerException e) {
            throw new QuartzSchedulerException(e.getMessage(), e);
        }
    }

    @Override
    public void runNow(String jobGroup, String jobName) {
        try {
            JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
            validateJobExists(jobKey);
            scheduler.triggerJob(jobKey);
        } catch (SchedulerException e) {
            throw new QuartzSchedulerException(e.getMessage(), e);
        }
    }

    @Override
    public void deleteJob(String jobGroup, String jobName) {
        try {
            JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
            validateJobExists(jobKey);
            scheduler.deleteJob(jobKey);
        } catch (SchedulerException e) {
            throw new QuartzSchedulerException(e.getMessage(), e);
        }
    }

    @Override
    public void retry(Long historyId) {
        QuartzJobExecutionHistory history = reader.findById(historyId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "실행 이력을 찾을 수 없습니다. id=" + historyId));

        if (history.getStatus() != QuartzJobExecutionStatus.FAILED) {
            throw new IllegalArgumentException("FAILED 상태의 실행 이력만 재실행할 수 있습니다.");
        }

        try {
            JobKey jobKey = JobKey.jobKey(history.getJobName(), history.getJobGroup());
            if (!scheduler.checkExists(jobKey)) {
                throw new IllegalArgumentException(
                        "Quartz에 등록된 Job이 없습니다. 다시 등록이 필요합니다. jobKey=" + jobKey);
            }
            scheduler.triggerJob(jobKey);
        } catch (SchedulerException e) {
            throw new QuartzSchedulerException(e.getMessage(), e);
        }
    }

    // ── private ─────────────────────────────────────────────────────────────

    private JobDataMap buildJobDataMap(QuartzJobUpsertCommand command) {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("jobType", command.jobType().name());
        jobDataMap.put("scheduleType", command.scheduleType().name());
        if (command.jobData() != null) {
            for (Map.Entry<String, String> entry : command.jobData().entrySet()) {
                jobDataMap.put(entry.getKey(), entry.getValue());
            }
        }
        return jobDataMap;
    }

    private void validateJobExists(JobKey jobKey) {
        try {
            if (!scheduler.checkExists(jobKey)) {
                throw new IllegalArgumentException("존재하지 않는 Job입니다. jobKey=" + jobKey);
            }
        } catch (SchedulerException e) {
            throw new QuartzSchedulerException(e.getMessage(), e);
        }
    }

    private Instant toInstant(java.util.Date date) {
        return date == null ? null : date.toInstant();
    }
}
