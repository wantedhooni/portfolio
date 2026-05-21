# Scheduler 모듈 사용 가이드

별도 워커 클러스터(`api-scheduler-worker`)에서 실제 Job을 실행하고,
`api-admin`은 control-plane 으로서 명령 발행과 이력 조회만 담당하는 구조입니다.

```
                       ┌─────────────────────┐
                       │  api-admin          │
                       │  (CONTROL mode)     │
   ─ REST ─────────▶   │  Scheduler.standby()│
                       │  Batch read+stop    │
                       └─────────┬───────────┘
                                 │  JDBC store
                       ┌─────────▼───────────┐
                       │  PostgreSQL appdb   │
                       │  QRTZ_*, BATCH_*    │
                       │  qrtz_job_history   │
                       │  batch_launch_req   │
                       └─────────▲───────────┘
                                 │
                       ┌─────────┴───────────┐
                       │  scheduler-worker   │ (별도 클러스터)
                       │  (WORKER mode)      │
                       │  Job class 보유     │
                       └─────────────────────┘
```

## 1. 워커 노드 애플리케이션

워커 노드는 별도의 Spring Boot 애플리케이션으로 만들고 본 `module/scheduler` 의존성을 추가합니다.

```groovy
// application/scheduler-worker/build.gradle
plugins { id 'org.springframework.boot' }

dependencies {
    implementation project(':module:core:core-web')
    implementation project(':module:business-logic')
    implementation project(':module:scheduler')

    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-flyway'
    runtimeOnly 'org.postgresql:postgresql'
}
```

### application.yml (워커)

```yaml
spring:
  application:
    name: scheduler-worker
  datasource:
    url: ${ADMIN_DB_URL:jdbc:postgresql://localhost:5431/appdb}
    username: postgres
    password: postgres
  quartz:
    job-store-type: jdbc
    jdbc:
      initialize-schema: never
  batch:
    job:
      enabled: false   # 자동 실행 차단. launch-request 폴러가 트리거
    jdbc:
      initialize-schema: never
  flyway:
    enabled: false     # DDL 은 api-admin 측 Flyway 가 관리

app:
  scheduler:
    mode: WORKER       # ★ 핵심 - Quartz Scheduler 가 start() 되어 Job 을 fire
    scheduler-instance-name: RevyCoreScheduler   # api-admin 과 동일해야 함!
    thread-pool-size: 20
```

> ⚠ `scheduler-instance-name` 은 api-admin 과 **반드시 동일**해야 같은 Quartz 클러스터로 인식됩니다.

## 2. 샘플 Quartz Job

```java
package com.revy.example.worker.quartz;

import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

public class OverdueInvoiceJob implements Job {
    @Override
    public void execute(JobExecutionContext context) {
        // billingCommand.markOverdueAll(LocalDate.now());
    }
}

@Configuration
class OverdueInvoiceJobConfig {
    @Bean
    public JobDetail overdueInvoiceJobDetail() {
        return JobBuilder.newJob(OverdueInvoiceJob.class)
                .withIdentity("overdueInvoiceJob", "billing")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger overdueInvoiceTrigger(JobDetail overdueInvoiceJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(overdueInvoiceJobDetail)
                .withIdentity("overdueInvoiceTrigger", "billing")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 1 * * ?")) // 매일 01:00
                .build();
    }
}
```

워커가 기동되면 위 Job 이 JDBC store 에 등록되고, api-admin 의 `GET /api/v1/scheduler/quartz/jobs` 에서 즉시 보입니다.

## 3. Quartz 실행 이력 기록 (JobListener)

`qrtz_job_history` 테이블은 사용자 정의이므로 워커가 직접 기록해야 합니다.

```java
package com.revy.example.worker.quartz;

import org.quartz.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;

@Component
public class HistoryRecordingJobListener implements JobListener {

    private final JdbcTemplate jdbc;
    public HistoryRecordingJobListener(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override public String getName() { return "history-recorder"; }

    private final ThreadLocal<Long> firedAt = new ThreadLocal<>();

    @Override
    public void jobToBeExecuted(JobExecutionContext ctx) {
        firedAt.set(System.currentTimeMillis());
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext ctx) {
        insert(ctx, null, "VETOED", null);
    }

    @Override
    public void jobWasExecuted(JobExecutionContext ctx, JobExecutionException e) {
        long start = firedAt.get() == null ? System.currentTimeMillis() : firedAt.get();
        long end   = System.currentTimeMillis();
        firedAt.remove();
        insert(ctx, end - start, e == null ? "SUCCESS" : "FAILED",
               e == null ? null : e.getMessage());
    }

    private void insert(JobExecutionContext ctx, Long runTime, String result, String msg) {
        try {
            jdbc.update(
                """
                INSERT INTO qrtz_job_history
                (instance_id, job_name, job_group, trigger_name, trigger_group,
                 fired_at, completed_at, run_time_ms, result, exception_message)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                ctx.getScheduler().getSchedulerInstanceId(),
                ctx.getJobDetail().getKey().getName(),
                ctx.getJobDetail().getKey().getGroup(),
                ctx.getTrigger().getKey().getName(),
                ctx.getTrigger().getKey().getGroup(),
                Timestamp.from(ctx.getFireTime().toInstant()),
                runTime == null ? null : Timestamp.from(Instant.now()),
                runTime,
                result,
                msg
            );
        } catch (Exception ignore) { /* 로깅만 */ }
    }
}
```

리스너 등록:

```java
@Bean
SchedulerFactoryBeanCustomizer registerListener(HistoryRecordingJobListener listener) {
    return factory -> factory.setGlobalJobListeners(listener);
}
```

## 4. 샘플 Spring Batch Job

```java
@Configuration
class DailySettlementJobConfig {

    @Bean
    public Job dailySettlementJob(JobRepository repo, Step aggregateStep) {
        return new JobBuilder("dailySettlementJob", repo)
                .start(aggregateStep)
                .build();
    }

    @Bean
    public Step aggregateStep(JobRepository repo, PlatformTransactionManager tx,
                              ItemReader<Trade> reader, ItemProcessor<Trade, Settlement> processor,
                              ItemWriter<Settlement> writer) {
        return new StepBuilder("aggregateStep", repo)
                .<Trade, Settlement>chunk(1000, tx)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }
}
```

## 5. Batch 실행 요청 큐 폴러 (워커)

api-admin → `POST /api/v1/scheduler/batch/launch-request` 호출 시 `batch_launch_request` 테이블에 INSERT.
워커는 다음과 같은 폴러로 처리:

```java
@Component
@RequiredArgsConstructor
public class BatchLaunchRequestPoller {

    private final JdbcTemplate jdbc;
    private final JobOperator jobOperator;
    private final JobRegistry jobRegistry;
    private final JobParametersConverter jobParametersConverter;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void poll() {
        var rows = jdbc.queryForList(
            "SELECT id, job_name, job_parameters FROM batch_launch_request " +
            "WHERE status = 'PENDING' ORDER BY id LIMIT 5 FOR UPDATE SKIP LOCKED");
        for (var row : rows) {
            Long id = ((Number) row.get("id")).longValue();
            try {
                // Spring Batch 6 모범 사례: Job 빈을 JobRegistry 에서 찾아
                // start(Job, JobParameters) 사용 (start(String, Properties) 는 폐기됨)
                Job job = jobRegistry.getJob((String) row.get("job_name"));
                JobParameters params = jobParametersConverter.getJobParameters(
                    parseProperties((String) row.get("job_parameters")));
                JobExecution exec = jobOperator.start(job, params);

                jdbc.update("UPDATE batch_launch_request SET status='PICKED', picked_at=now(), " +
                            "picked_by=?, execution_id=? WHERE id=?",
                            instanceName, exec.getId(), id);
            } catch (Exception e) {
                jdbc.update("UPDATE batch_launch_request SET status='FAILED', error_message=? WHERE id=?",
                            e.getMessage(), id);
            }
        }
    }
}
```

## 6. DDL 관리

- Quartz 표준 테이블 + 사용자 정의 `qrtz_job_history` + Spring Batch 5/6 표준 테이블 + `batch_launch_request`
- 위 모두 `api-admin` 의 Flyway 마이그레이션 `V20260521120000__create_scheduler_tables.sql` 으로 관리됨
- 워커 노드는 `spring.flyway.enabled=false` 로 두고 마이그레이션을 건너뜀

## 7. 권한

- `SCHEDULER_READ` — 조회 API
- `SCHEDULER_WRITE` — 제어 (trigger/pause/resume/delete, batch launch/stop/abandon/restart)

`SUPER_ADMIN` 역할에 자동 포함되며, 별도 역할에 부여해 운영팀에 위임 가능.

## 8. 클러스터 운영 체크리스트

- 같은 `scheduler-instance-name`, 같은 DB 를 공유해야 클러스터 인식
- 워커는 horizontal 하게 N개 띄울 수 있음 — Quartz JobStore 가 lock 으로 직렬 실행 보장
- api-admin 은 `CONTROL` 모드 → 워커 0대 일 때도 안전 (Job 미발화)
- 시스템 시계 동기화 필수 (NTP) — Quartz misfire 판정에 영향
