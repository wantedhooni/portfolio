package com.revy.example.scheduler.quartz.impl;

import com.revy.example.core.error.BusinessException;
import com.revy.example.core.error.ErrorCode;
import com.revy.example.scheduler.quartz.QuartzReader;
import com.revy.example.scheduler.quartz.dto.QuartzExecutionResult;
import com.revy.example.scheduler.quartz.dto.QuartzJobResult;
import com.revy.example.scheduler.quartz.dto.QuartzTriggerResult;
import lombok.RequiredArgsConstructor;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class QuartzReaderImpl implements QuartzReader {

    private final Scheduler scheduler;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<QuartzJobResult> listJobs() {
        try {
            List<QuartzJobResult> result = new ArrayList<>();
            for (String group : scheduler.getJobGroupNames()) {
                for (JobKey key : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(group))) {
                    findJob(key.getGroup(), key.getName()).ifPresent(result::add);
                }
            }
            result.sort(Comparator.comparing(QuartzJobResult::group).thenComparing(QuartzJobResult::name));
            return result;
        } catch (SchedulerException e) {
            throw new BusinessException(ErrorCode.SCHEDULER_OPERATION_FAILED, e.getMessage());
        }
    }

    @Override
    public Optional<QuartzJobResult> findJob(String group, String name) {
        try {
            JobKey key = JobKey.jobKey(name, group);
            JobDetail detail = scheduler.getJobDetail(key);
            if (detail == null) return Optional.empty();

            List<? extends Trigger> triggers = scheduler.getTriggersOfJob(key);
            List<QuartzTriggerResult> triggerResults = new ArrayList<>();
            for (Trigger t : triggers) triggerResults.add(toTriggerResult(t));

            Map<String, Object> data = new HashMap<>(detail.getJobDataMap().getWrappedMap());

            return Optional.of(new QuartzJobResult(
                    key.getName(),
                    key.getGroup(),
                    detail.getDescription(),
                    detail.getJobClass().getName(),
                    detail.isDurable(),
                    detail.isConcurrentExecutionDisallowed(),
                    detail.isPersistJobDataAfterExecution(),
                    detail.requestsRecovery(),
                    data,
                    triggerResults
            ));
        } catch (SchedulerException e) {
            throw new BusinessException(ErrorCode.SCHEDULER_OPERATION_FAILED, e.getMessage());
        }
    }

    @Override
    public List<QuartzTriggerResult> listTriggers() {
        try {
            List<QuartzTriggerResult> result = new ArrayList<>();
            for (String group : scheduler.getTriggerGroupNames()) {
                for (TriggerKey key : scheduler.getTriggerKeys(GroupMatcher.triggerGroupEquals(group))) {
                    Trigger t = scheduler.getTrigger(key);
                    if (t != null) result.add(toTriggerResult(t));
                }
            }
            result.sort(Comparator.comparing(QuartzTriggerResult::group).thenComparing(QuartzTriggerResult::name));
            return result;
        } catch (SchedulerException e) {
            throw new BusinessException(ErrorCode.SCHEDULER_OPERATION_FAILED, e.getMessage());
        }
    }

    @Override
    public Page<QuartzExecutionResult> searchExecutions(Pageable pageable, String jobName, String result) {
        StringBuilder where = new StringBuilder(" WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (jobName != null && !jobName.isBlank()) {
            where.append(" AND job_name = ? ");
            params.add(jobName);
        }
        if (result != null && !result.isBlank()) {
            where.append(" AND result = ? ");
            params.add(result);
        }

        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM qrtz_job_history " + where, Long.class, params.toArray());
        if (total == null || total == 0L) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        params.add(pageable.getPageSize());
        params.add(pageable.getOffset());
        List<QuartzExecutionResult> rows = jdbcTemplate.query(
                "SELECT id, job_name, job_group, trigger_name, trigger_group, " +
                        "       fired_at, completed_at, run_time_ms, result, exception_message, instance_id " +
                        " FROM qrtz_job_history " + where +
                        " ORDER BY fired_at DESC LIMIT ? OFFSET ?",
                (rs, i) -> new QuartzExecutionResult(
                        rs.getLong("id"),
                        rs.getString("job_name"),
                        rs.getString("job_group"),
                        rs.getString("trigger_name"),
                        rs.getString("trigger_group"),
                        toInstant(rs.getTimestamp("fired_at")),
                        toInstant(rs.getTimestamp("completed_at")),
                        (Long) rs.getObject("run_time_ms"),
                        rs.getString("result"),
                        rs.getString("exception_message"),
                        rs.getString("instance_id")
                ),
                params.toArray());
        return new PageImpl<>(rows, pageable, total);
    }

    @Override
    public List<QuartzExecutionResult> listCurrentlyExecuting() {
        try {
            List<JobExecutionContext> running = scheduler.getCurrentlyExecutingJobs();
            List<QuartzExecutionResult> result = new ArrayList<>();
            for (JobExecutionContext ctx : running) {
                result.add(new QuartzExecutionResult(
                        null,
                        ctx.getJobDetail().getKey().getName(),
                        ctx.getJobDetail().getKey().getGroup(),
                        ctx.getTrigger().getKey().getName(),
                        ctx.getTrigger().getKey().getGroup(),
                        toInstant(ctx.getFireTime()),
                        null,
                        null,
                        "RUNNING",
                        null,
                        ctx.getScheduler().getSchedulerInstanceId()
                ));
            }
            return result;
        } catch (SchedulerException e) {
            throw new BusinessException(ErrorCode.SCHEDULER_OPERATION_FAILED, e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────

    private QuartzTriggerResult toTriggerResult(Trigger t) throws SchedulerException {
        String type = "SIMPLE";
        String cron = null;
        Long  interval = null;
        Integer repeat = null;
        if (t instanceof CronTrigger c) {
            type = "CRON";
            cron = c.getCronExpression();
        } else if (t instanceof SimpleTrigger s) {
            interval = s.getRepeatInterval();
            repeat   = s.getRepeatCount();
        }
        Trigger.TriggerState state = scheduler.getTriggerState(t.getKey());
        return new QuartzTriggerResult(
                t.getKey().getName(),
                t.getKey().getGroup(),
                t.getJobKey().getName(),
                t.getJobKey().getGroup(),
                t.getDescription(),
                type,
                cron,
                interval,
                repeat,
                toInstant(t.getNextFireTime()),
                toInstant(t.getPreviousFireTime()),
                toInstant(t.getStartTime()),
                toInstant(t.getEndTime()),
                state.name()
        );
    }

    private static Instant toInstant(Date d) {
        return d == null ? null : d.toInstant();
    }

    private static Instant toInstant(java.sql.Timestamp ts) {
        return ts == null ? null : ts.toInstant();
    }
}
