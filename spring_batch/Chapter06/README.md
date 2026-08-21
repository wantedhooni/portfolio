# Spring Batch Chapter 06

《The Definitive Guide to Spring Batch》(한국어판: 《스프링 배치 완벽 가이드 2판》)의 Job 스케줄링과 커스텀 ItemReader 예제를 Kotlin, Spring Batch 6, Quartz로 정리한 학습용 프로젝트입니다.

## 학습 내용

- Quartz로 Spring Batch Job 주기적 실행
- `JobOperator.startNextInstance` 및 `RunIdIncrementer`를 이용한 반복 실행
- `ItemStreamReader`를 구현한 커스텀 거래 리더
- `FlatFileItemReader`의 Job Parameter 기반 리소스 바인딩
- `JdbcBatchItemWriter`를 이용한 거래 저장
- `ItemProcessor`와 DAO를 이용한 계좌 잔액 계산
- Flyway를 통한 Spring Batch·Quartz 메타데이터 스키마 관리

## 기술 스택

| 구분 | 버전/구성 |
| --- | --- |
| Kotlin | 2.2.21 |
| Java | 21 |
| Spring Boot | 4.0.7 |
| Spring Batch | Spring Boot 관리 버전(Spring Batch 6.0.4) |
| Quartz | Spring Boot Starter Quartz |
| Flyway | Spring Boot Starter Flyway |
| Gradle | 9.6.0 Wrapper |
| Database | PostgreSQL |

## 프로젝트 구조

```text
src/main
├── kotlin/com/revy/batch/chapter06
│   ├── Chapter06Application.kt
│   ├── batch
│   │   ├── TransactionReader.kt
│   │   ├── TransactionApplierProcessor.kt
│   │   └── job
│   │       ├── SampleBatchJob.kt
│   │       └── TransactionProcessingJob.kt
│   ├── domain
│   │   ├── AccountSummary.kt
│   │   ├── Transaction.kt
│   │   ├── TransactionDao.kt
│   │   └── support/TransactionDaoSupport.kt
│   └── quartz
│       ├── BatchScheduledJob.kt
│       └── config/QuartzConfiguration.kt
└── resources
    ├── application.yml
    ├── db/migration
    │   ├── V1__create_spring_batch_metadata.sql
    │   └── V2__create_quartz_metadata.sql
    ├── input/transactionFile.csv
    ├── schema.sql
    └── data.sql
```

## Quartz Batch Job 실행

`SampleBatchJob`은 `job` 이름의 Job과 `step1` Step을 구성합니다. `step1`은 `step1 ran!`을 출력하고 `FINISHED`를 반환하는 단순 Tasklet입니다.

```text
Quartz Trigger
    → BatchScheduledJob
        → JobOperator.startNextInstance(job)
            → job
                → step1
```

`QuartzConfiguration`은 `BatchScheduledJob`을 durable JobDetail로 등록하고 5초 간격, 반복 횟수 4로 SimpleTrigger를 생성합니다. Quartz의 repeat count는 첫 실행 이후의 반복 횟수이므로 트리거는 총 5회 발생합니다. `RunIdIncrementer`는 각 실행에 새 `run.id`를 부여합니다.

`spring.batch.job.enabled` 설정은 `false`이므로 애플리케이션 시작 시 Batch Job을 바로 실행하지 않고 Quartz가 실행을 담당합니다. Quartz Job Store는 PostgreSQL을 사용합니다.

## 거래 처리 구성요소

| 구성요소 | 현재 구현 |
| --- | --- |
| `fileItemReader` | Job Parameter `transactionFile`에서 CSV 리소스를 받아 `FieldSet`으로 읽음 |
| `TransactionReader` | `FieldSet`의 계좌번호, 일시, 금액을 `Transaction`으로 변환 |
| `transactionWriter` | `JdbcBatchItemWriter`로 계좌 ID를 조회한 뒤 거래 저장 |
| `TransactionApplierProcessor` | 계좌별 거래를 DAO로 조회해 잔액에 반영 |
| `TransactionDaoSupport` | `JdbcTemplate`으로 계좌번호에 해당하는 거래 조회 |

CSV 파일의 거래 행은 다음 형식입니다.

```text
accountNumber,timestamp,amount
```

`fileItemReader`는 `@StepScope`로 생성되므로 실제 Job에서 사용할 때는 `transactionFile`을 Job Parameter로 전달해야 합니다.

```text
transactionFile=classpath:input/transactionFile.csv
```

## Flyway 및 데이터베이스

| 마이그레이션 | 역할 |
| --- | --- |
| `V1__create_spring_batch_metadata.sql` | Batch Job/Step 실행 이력 테이블과 시퀀스 생성 |
| `V2__create_quartz_metadata.sql` | Quartz Job, Trigger, Scheduler, Lock 테이블과 인덱스 생성 |

Batch와 Quartz의 자체 JDBC 스키마 초기화는 `never`로 설정되어 있으며 Flyway가 메타데이터 스키마를 관리합니다. 기존 스키마에서도 V1부터 적용할 수 있도록 baseline version은 `0`입니다.

`schema.sql`은 `account_summary`와 `account_transaction` 테이블을 정의하고 `data.sql`은 계좌 초기 데이터를 제공합니다. 현재 `spring.sql.init.mode` 설정은 주석 처리되어 있어 두 파일은 애플리케이션 시작 시 강제 실행되지 않습니다.

## 실행

로컬 PostgreSQL에 다음 접속 정보가 준비되어 있어야 합니다.

```yaml
url: jdbc:postgresql://localhost:5432/app
username: app
password: app
```

프로젝트 디렉터리에서 애플리케이션을 실행합니다.

```bash
./gradlew bootRun
```

컴파일만 검증하려면 다음 명령을 사용합니다.

```bash
./gradlew compileKotlin
```

## 현재 구현 주의사항

- `TransactionProcessingJob`에는 Reader와 Writer Bean만 정의되어 있으며, 이를 사용하는 Step과 Job은 아직 구성되지 않았습니다.
- `TransactionReader`는 25개의 거래를 읽은 뒤 학습용 `ParseException`을 의도적으로 발생시킵니다.
- CSV의 마지막 레코드 수 `99`를 검증하려는 필드가 있지만, 현재 EOF 분기에서 null `FieldSet`을 읽으므로 예상 건수가 설정되지 않습니다. `stepExecution`도 외부에서 주입되는 코드가 없습니다.
- 날짜 패턴은 `yyyy-MM-DD HH:mm:ss`로 작성되어 있습니다. 일(day of month)을 의도했다면 `DD`가 아닌 `dd`를 사용해야 합니다.
- Writer는 `TRANSACTION` 테이블에 저장하지만 `schema.sql`이 생성하는 테이블 이름은 `account_transaction`입니다.
- `TransactionDaoSupport`도 `transaction`을 조회하므로 `schema.sql`의 `account_transaction`과 이름이 일치하지 않습니다.
- `AccountSummary.addCurrentBalance`의 `=+` 연산은 누적 덧셈이 아니라 전달된 금액으로 잔액을 대입합니다.
