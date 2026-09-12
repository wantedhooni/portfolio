package com.revy.executor.service.dto;


public class QuartzDtos {

    public record ScheduleCronRequest(
            String jobName,
            String jobGroup,
            String triggerName,
            String triggerGroup,
            String cron
    ) {}

    public record JobKeyDto(String name, String group) {}
    public record TriggerKeyDto(String name, String group) {}
}
