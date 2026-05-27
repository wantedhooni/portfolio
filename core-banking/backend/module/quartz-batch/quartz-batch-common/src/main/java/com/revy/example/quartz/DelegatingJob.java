package com.revy.example.quartz;

import com.revy.example.quartz.enums.JobType;
import com.revy.example.quartz.registry.JobClassRegistry;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

/**
 * 모든 {@link JobType}을 하나의 클래스로 위임하는 Quartz Job 프록시.
 *
 * <p>Quartz DB에는 항상 이 클래스명으로 저장됩니다.
 * 실행 시점에 {@link JobDataMap}의 {@code jobType} 값을 읽어
 * {@link JobClassRegistry}로 실제 Job 빈을 조회한 뒤 위임합니다.
 *
 * <p><b>관심사 분리:</b>
 * <ul>
 *   <li>이 클래스는 라우팅 인프라입니다. 비즈니스 의존성이 없습니다.</li>
 *   <li>실제 실행 bean({@code ExchangeRateRefreshJob} 등)은 {@code quartz-batch-executor}에만 존재합니다.</li>
 *   <li>관리 노드(api-admin)는 이 클래스로 Job을 등록하고, 실행은 server-executor가 담당합니다.</li>
 * </ul>
 *
 * <p>Spring Boot Quartz 스타터의 {@code AutowireCapableBeanJobFactory}가
 * Job 실행마다 새 인스턴스를 생성하고 {@code @Autowired} 필드를 주입합니다.
 */
@Slf4j
public class DelegatingJob implements Job {

    @Autowired
    private JobClassRegistry jobClassRegistry;

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap  = context.getMergedJobDataMap();
        String     typeName = dataMap.getString("jobType");

        if (typeName == null || typeName.isBlank()) {
            throw new JobExecutionException("JobDataMap에 'jobType'이 없습니다.");
        }

        JobType jobType;
        try {
            jobType = JobType.valueOf(typeName);
        } catch (IllegalArgumentException e) {
            throw new JobExecutionException("알 수 없는 JobType: " + typeName, e);
        }

        Class<? extends Job> realClass = jobClassRegistry.resolve(jobType);
        Job realJob = applicationContext.getBean(realClass);

        log.debug("[DelegatingJob] 위임 실행: jobType={} → {}", jobType, realClass.getSimpleName());
        realJob.execute(context);
    }
}
