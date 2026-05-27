package com.revy.example.quartz.enums;

import com.revy.example.common.enums.ExposedEnum;

public enum ScheduleType implements ExposedEnum {

    CRON  ("CRON 스케줄"),
    SIMPLE("반복 주기"),
    ONCE  ("1회 실행");

    private final String label;

    ScheduleType(String label) {
        this.label = label;
    }
}
