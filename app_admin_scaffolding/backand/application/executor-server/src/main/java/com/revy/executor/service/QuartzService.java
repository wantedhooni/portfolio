package com.revy.executor.service;

import com.revy.executor.service.dto.QuartzDtos;
import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class QuartzService {

    private final Scheduler scheduler;

    public QuartzService(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public List<JobKey> listAllJobs() throws SchedulerException {
        List<JobKey> out = new ArrayList<>();
        for (String group : scheduler.getJobGroupNames()) {
            out.addAll(scheduler.getJobKeys(GroupMatcher.jobGroupEquals(group)));
        }
        return out;
    }

    public List<? extends Trigger> getTriggers(JobKey jobKey) throws SchedulerException {
        return scheduler.getTriggersOfJob(jobKey);
    }

    public void triggerNow(JobKey jobKey) throws SchedulerException {
        scheduler.triggerJob(jobKey);
    }

    public void pauseJob(JobKey jobKey) throws SchedulerException {
        scheduler.pauseJob(jobKey);
    }

    public void resumeJob(JobKey jobKey) throws SchedulerException {
        scheduler.resumeJob(jobKey);
    }

    public void deleteJob(JobKey jobKey) throws SchedulerException {
        scheduler.deleteJob(jobKey);
    }

    /**
     * - 이미 존재하는 JobDetail이면 재사용
     * - Trigger는 upsert(존재하면 reschedule)
     */
    public void upsertCronSchedule(
            QuartzDtos.ScheduleCronRequest req,
            Class<? extends Job> jobClass
    ) throws SchedulerException {

        JobKey jobKey = new JobKey(req.jobName(), req.jobGroup());
        TriggerKey triggerKey = new TriggerKey(req.triggerName(), req.triggerGroup());

        JobDetail jobDetail;
        if (scheduler.checkExists(jobKey)) {
            jobDetail = scheduler.getJobDetail(jobKey);
        } else {
            jobDetail = JobBuilder.newJob(jobClass)
                                  .withIdentity(jobKey)
                                  .storeDurably(true)
                                  .build();
            scheduler.addJob(jobDetail, false);
        }

        CronScheduleBuilder cron = CronScheduleBuilder.cronSchedule(req.cron());

        Trigger newTrigger = TriggerBuilder.newTrigger()
                                           .withIdentity(triggerKey)
                                           .forJob(jobDetail)
                                           .withSchedule(cron)
                                           .build();

        if (scheduler.checkExists(triggerKey)) {
            scheduler.rescheduleJob(triggerKey, newTrigger);
        } else {
            scheduler.scheduleJob(newTrigger);
        }
    }
}
