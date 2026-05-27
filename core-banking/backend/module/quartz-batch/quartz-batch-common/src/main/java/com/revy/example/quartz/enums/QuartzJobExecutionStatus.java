package com.revy.example.quartz.enums;

import com.revy.example.common.enums.ExposedEnum;

public enum QuartzJobExecutionStatus implements ExposedEnum {

    RUNNING("실행 중"),
    SUCCESS("성공"),
    FAILED ("실패"),
    VETOED ("거부");

    private final String label;

    QuartzJobExecutionStatus(String label) {
        this.label = label;
    }
}
