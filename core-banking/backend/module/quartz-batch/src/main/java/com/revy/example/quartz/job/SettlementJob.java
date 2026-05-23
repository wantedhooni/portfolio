package com.revy.example.quartz.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class SettlementJob implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String market = context.getMergedJobDataMap().getString("market");

        // TODO: 실제 정산 로직 호출
        // settlementService.execute(market);
    }
}