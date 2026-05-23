package com.revy.example.quartz.handler;

import com.revy.example.quartz.dto.QuartzJobResult;
import com.revy.example.quartz.dto.QuartzJobUpsertCommand;
import com.revy.example.quartz.dto.QuartzRunningJobResult;
import org.quartz.JobDataMap;
import org.quartz.SchedulerException;

import java.util.List;

public interface QuartzJobHandler {
    void createJob(QuartzJobUpsertCommand command) throws SchedulerException;

    void updateJob(String jobGroup, String jobName, QuartzJobUpsertCommand command) throws SchedulerException;

    void updateScheduleOnly(String jobGroup, String jobName, QuartzJobUpsertCommand command) throws SchedulerException;

    List<QuartzJobResult> findAllJobs() throws SchedulerException;

    List<QuartzRunningJobResult> findRunningJobs() throws SchedulerException;

    void pauseJob(String jobGroup, String jobName) throws SchedulerException;

    void resumeJob(String jobGroup, String jobName) throws SchedulerException;

    void runNow(String jobGroup, String jobName) throws SchedulerException;

    void deleteJob(String jobGroup, String jobName) throws SchedulerException;

    JobDataMap toJobDataMap(QuartzJobUpsertCommand command);
}
