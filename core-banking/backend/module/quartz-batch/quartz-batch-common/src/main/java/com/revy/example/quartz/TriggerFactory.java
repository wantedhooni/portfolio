package com.revy.example.quartz;

import com.revy.example.quartz.dto.QuartzJobUpsertCommand;
import org.quartz.*;

import java.util.Date;

public final class TriggerFactory {

    private TriggerFactory() {
    }

    public static Trigger createTrigger(String jobName, String jobGroup, QuartzJobUpsertCommand command) {
        TriggerKey triggerKey = TriggerKey.triggerKey(jobName + "Trigger", jobGroup);

        JobKey jobKey = JobKey.jobKey(jobName, jobGroup);

        TriggerBuilder<Trigger> builder = TriggerBuilder.newTrigger()
            .withIdentity(triggerKey)
            .forJob(jobKey)
            .withDescription(command.description());

        if (command.startAt() != null) {
            builder.startAt(Date.from(command.startAt()
                                          .toInstant()));
        } else {
            builder.startNow();
        }

        return switch (command.scheduleType()) {
            case CRON -> createCronTrigger(builder, command);
            case SIMPLE -> createSimpleTrigger(builder, command);
            case ONCE -> createOnceTrigger(builder);
        };
    }

    private static Trigger createCronTrigger(TriggerBuilder<Trigger> builder, QuartzJobUpsertCommand command) {
        if (command.cronExpression() == null || command.cronExpression()
            .isBlank()) {
            throw new IllegalArgumentException("CRON 스케줄은 cronExpression이 필요합니다.");
        }

        return builder.withSchedule(CronScheduleBuilder.cronSchedule(command.cronExpression())
                                        .withMisfireHandlingInstructionDoNothing())
            .build();
    }

    private static Trigger createSimpleTrigger(TriggerBuilder<Trigger> builder, QuartzJobUpsertCommand command) {
        if (command.repeatIntervalMs() == null || command.repeatIntervalMs() <= 0) {
            throw new IllegalArgumentException("SIMPLE 스케줄은 repeatIntervalMs가 필요합니다.");
        }

        int repeatCount = command.repeatCount() == null ? SimpleTrigger.REPEAT_INDEFINITELY : command.repeatCount();

        return builder.withSchedule(SimpleScheduleBuilder.simpleSchedule()
                                        .withIntervalInMilliseconds(command.repeatIntervalMs())
                                        .withRepeatCount(repeatCount)
                                        .withMisfireHandlingInstructionNextWithRemainingCount())
            .build();
    }

    private static Trigger createOnceTrigger(TriggerBuilder<Trigger> builder) {
        return builder.build();
    }
}