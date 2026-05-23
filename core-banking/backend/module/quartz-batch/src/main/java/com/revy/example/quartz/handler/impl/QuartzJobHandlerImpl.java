package com.revy.example.quartz.handler.impl;

import com.revy.example.quartz.JobTypeResolver;
import com.revy.example.quartz.TriggerFactory;
import com.revy.example.quartz.dto.QuartzJobResult;
import com.revy.example.quartz.dto.QuartzJobUpsertCommand;
import com.revy.example.quartz.dto.QuartzRunningJobResult;
import com.revy.example.quartz.handler.QuartzJobHandler;
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
public class QuartzJobHandlerImpl implements QuartzJobHandler {

    private final Scheduler scheduler;

    public QuartzJobHandlerImpl(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public void createJob(QuartzJobUpsertCommand command) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(command.jobName(), command.jobGroup());

        if (scheduler.checkExists(jobKey)) {
            throw new IllegalArgumentException("이미 존재하는 Job입니다. jobKey=" + jobKey);
        }

        Class<? extends Job> jobClass = JobTypeResolver.resolve(command.jobType());
        JobDataMap jobDataMap = toJobDataMap(command);

        JobDetail jobDetail = JobBuilder.newJob(jobClass)
            .withIdentity(jobKey)
            .withDescription(command.description())
            .usingJobData(jobDataMap)
            .storeDurably(false)
            .build();

        Trigger trigger = TriggerFactory.createTrigger(command.jobName(), command.jobGroup(), command);

        scheduler.scheduleJob(jobDetail, trigger);
    }

    @Override
    public void updateJob(String jobGroup, String jobName, QuartzJobUpsertCommand command) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(jobName, jobGroup);

        if (!scheduler.checkExists(jobKey)) {
            throw new IllegalArgumentException("존재하지 않는 Job입니다. jobKey=" + jobKey);
        }

        scheduler.deleteJob(jobKey);

        QuartzJobUpsertCommand normalizedCommand = new QuartzJobUpsertCommand(jobName, jobGroup, command.jobType(),
                                                                              command.scheduleType(),
                                                                              command.cronExpression(),
                                                                              command.repeatIntervalMs(),
                                                                              command.repeatCount(), command.startAt(),
                                                                              command.description(), command.jobData());

        createJob(normalizedCommand);
    }

    @Override
    public void updateScheduleOnly(String jobGroup, String jobName,
                                   QuartzJobUpsertCommand command) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(jobName, jobGroup);

        validateJobExists(jobKey);

        Trigger oldTrigger = scheduler.getTriggersOfJob(jobKey)
            .stream()
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Job에 연결된 Trigger가 없습니다."));

        Trigger newTrigger = TriggerFactory.createTrigger(jobName, jobGroup, command);

        scheduler.rescheduleJob(oldTrigger.getKey(), newTrigger);
    }

    @Override
    public List<QuartzJobResult> findAllJobs() throws SchedulerException {
        List<QuartzJobResult> result = new ArrayList<>();

        for (String groupName : scheduler.getJobGroupNames()) {
            for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
                JobDetail jobDetail = scheduler.getJobDetail(jobKey);

                for (Trigger trigger : scheduler.getTriggersOfJob(jobKey)) {
                    Trigger.TriggerState state = scheduler.getTriggerState(trigger.getKey());

                    result.add(new QuartzJobResult(jobKey.getName(), jobKey.getGroup(), trigger.getKey()
                        .getName(), trigger.getKey()
                                                       .getGroup(), state.name(),
                                                   trigger instanceof CronTrigger ? "CRON" : trigger.getClass()
                                                       .getSimpleName(), jobDetail.getDescription(),
                                                   toInstant(trigger.getPreviousFireTime()),
                                                   toInstant(trigger.getNextFireTime())));
                }
            }
        }

        return result;
    }

    @Override
    public List<QuartzRunningJobResult> findRunningJobs() throws SchedulerException {
        return scheduler.getCurrentlyExecutingJobs()
            .stream()
            .map(context -> {
                JobKey jobKey = context.getJobDetail()
                    .getKey();
                TriggerKey triggerKey = context.getTrigger()
                    .getKey();

                long runningTimeMs = context.getFireTime() == null ? 0L : System.currentTimeMillis() - context.getFireTime()
                    .getTime();

                return new QuartzRunningJobResult(jobKey.getName(), jobKey.getGroup(), triggerKey.getName(),
                                                  triggerKey.getGroup(), toInstant(context.getFireTime()),
                                                  toInstant(context.getScheduledFireTime()), runningTimeMs);
            })
            .toList();
    }

    @Override
    public void pauseJob(String jobGroup, String jobName) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
        validateJobExists(jobKey);
        scheduler.pauseJob(jobKey);
    }

    @Override
    public void resumeJob(String jobGroup, String jobName) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
        validateJobExists(jobKey);
        scheduler.resumeJob(jobKey);
    }

    @Override
    public void runNow(String jobGroup, String jobName) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
        validateJobExists(jobKey);
        scheduler.triggerJob(jobKey);
    }

    @Override
    public void deleteJob(String jobGroup, String jobName) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
        validateJobExists(jobKey);
        scheduler.deleteJob(jobKey);
    }

    @Override
    public JobDataMap toJobDataMap(QuartzJobUpsertCommand command) {
        JobDataMap jobDataMap = new JobDataMap();

        jobDataMap.put("jobType", command.jobType()
            .name());
        jobDataMap.put("scheduleType", command.scheduleType()
            .name());

        if (command.jobData() != null) {
            for (Map.Entry<String, String> entry : command.jobData()
                .entrySet()) {
                jobDataMap.put(entry.getKey(), entry.getValue());
            }
        }

        return jobDataMap;
    }

    private void validateJobExists(JobKey jobKey) throws SchedulerException {
        if (!scheduler.checkExists(jobKey)) {
            throw new IllegalArgumentException("존재하지 않는 Job입니다. jobKey=" + jobKey);
        }
    }

    private Instant toInstant(java.util.Date date) {
        return date == null ? null : date.toInstant();
    }
}