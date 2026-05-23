package com.revy.example.quartz;

import com.revy.example.quartz.enums.JobType;
import com.revy.example.quartz.job.SettlementJob;
import org.quartz.Job;

public final class JobTypeResolver {

    private JobTypeResolver() {
    }

    public static Class<? extends Job> resolve(JobType jobType) {
        return switch (jobType) {
            case SETTLEMENT -> SettlementJob.class;
            // case REPORT -> ReportJob.class;
            //case NOTIFICATION -> NotificationJob.class;
            // case API_CALL -> ApiCallJob.class;
            default -> throw new IllegalArgumentException("Unsupported job type: " + jobType);
        };
    }
}