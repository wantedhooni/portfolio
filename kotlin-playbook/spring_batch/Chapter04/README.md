# Spring Batch Chapter 04

《The Definitive Guide to Spring Batch》(한국어판: 《스프링 배치 완벽 가이드 2판》)의 Job과 Step 구성 예제를 Kotlin 및 Spring Batch 6 API로 정리한 학습용 프로젝트입니다.

## 학습 내용

- `JobBuilder`, `StepBuilder`를 이용한 Job/Step 구성
- Job 파라미터 검증, 증가 전략 및 `@StepScope` 지연 바인딩
- Job/Step 실행 전후 리스너
- Tasklet, Chunk, Flow, JobStep 기반 처리
- 조건부 전이와 재시작 지점 지정
- `ExecutionContext`를 이용한 실행 상태 공유
- 기존 메서드, `Callable`, 시스템 명령을 Tasklet으로 연결

## 기술 스택

| 구분 | 버전/구성 |
| --- | --- |
| Kotlin | 2.2.21 |
| Java | 21 |
| Spring Boot | 4.0.7 |
| Spring Batch | Spring Boot 관리 버전(Spring Batch 6 계열) |
| Gradle | 9.6.0 Wrapper |
| Database | PostgreSQL |

Spring Batch 메타데이터는 JDBC 기반 `JobRepository`에 저장됩니다. JPA와 Querydsl 의존성도 포함되어 있지만, 현재 Chapter 04 예제에는 Entity나 Querydsl 조회 코드는 없습니다.

## 프로젝트 구조

```text
src/main/kotlin/com/revy/batch/chapter_04
├── Application.kt                 # 기본 애플리케이션 진입점
├── batch/                         # Tasklet, Listener, Validator 등 공통 배치 구성요소
├── common/                        # 공통 로깅 확장
├── config/                        # Spring Batch 활성화 설정
├── job/                           # 주제별 Job 예제와 독립 실행 진입점
└── service/                       # MethodInvokingTasklet 예제 서비스
```

## Job 예제

| 파일 | Job 이름 | 핵심 내용 |
| --- | --- | --- |
| `HelloWorldJob.kt` | `basicJob` | 필수 `fileName` 검증, 선택 `name`/`currentDate`, 날짜 파라미터 증가, Job 리스너, Step Scope 파라미터 바인딩 |
| `ExecutionContextJob.kt` | `helloWorldBatchJob` | Job 파라미터 `name`을 출력하고 Step `ExecutionContext`에 `user.name`으로 저장 |
| `ChunkJob.kt` | `chunkBasedJob` | UUID 100,000개를 읽어 1,000개 단위 Chunk로 출력하고 Step 리스너 적용 |
| `ConditionalJob.kt` | `conditionalJob` | Step 종료 상태에 따른 전이와 `stopAndRestart` 구성. 현재 `firstStep`은 의도적으로 실패 |
| `FlowJob.kt` | `conditionalStepLogicJob` | 파일 적재와 상태 갱신 Step을 `Flow`로 묶어 하나의 FlowStep으로 실행 |
| `JobJob.kt` | `conditionalStepLogicJob` | 전처리 Child Job을 `JobStep`으로 감싸 Parent Job에서 실행 |
| `MethodInvokingTaskletConfiguration.kt` | `methodInvokingJob` | Job 파라미터 `message`를 `CustomService.serviceMethod` 인자로 전달 |
| `CallableTaskletConfiguration.kt` | `callableJob` | `CallableTaskletAdapter`로 별도 스레드에서 작업 실행 |
| `SystemCommandJob.kt` | `systemCommandJob` | `SystemCommandTasklet`으로 시스템 명령 실행 |
| `AdvancedSystemCommandJob.kt` | `systemCommandJob` | 작업 디렉터리, 비동기 실행기, 종료 코드 매퍼 및 종료 확인 주기를 추가한 시스템 명령 예제 |

### 주요 실행 흐름

```text
FlowJob
preProcessingFlow(loadFileStep → loadCustomerStep → updateStartStep)
    → runBatch

JobJob
Parent Job
    → initializeBatch(JobStep)
        → Child Job(loadFileStep → loadCustomerStep → updateStartStep)
    → runBatch
```

## 공통 배치 구성요소

| 클래스 | 역할 |
| --- | --- |
| `DailyJobTimestamper` | `currentDate`를 추가해 새로운 Job Instance 파라미터 생성 |
| `ParameterValidator` | `fileName` 존재 여부와 `.csv` 확장자 검증 |
| `JobLoggerListener` | Job 시작과 종료 상태 로깅 |
| `LoggingStepStartStopListener` | Step 시작과 종료 로깅 |
| `HelloWorldTasklet` | `name` 출력 및 Step `ExecutionContext` 기록 |
| `RandomChunkSizePolicy` | 매 Chunk마다 0~19 범위의 임의 완료 크기 결정 |
| `RandomDecider` | 무작위로 `COMPLETED` 또는 `FAILED` 상태 반환 |

`ChunkJob`에는 복합 완료 정책과 임의 Chunk 정책 Bean도 정의되어 있지만, 현재 `chunkStep`은 고정 크기 `1,000`을 직접 사용합니다. `ConditionalJob`의 `failureStep`과 `RandomDecider` 역시 Bean으로 제공되지만 현재 Job 흐름에는 연결되어 있지 않습니다.

## 실행 환경

로컬 PostgreSQL에 다음 접속 정보가 준비되어 있어야 합니다.

```yaml
url: jdbc:postgresql://localhost:5432/app
username: app
password: app
```

애플리케이션 설정은 실행 시 Hibernate 스키마를 생성하고 종료 시 제거하는 `create-drop`을 사용합니다. Spring Batch 메타데이터 테이블 초기화가 필요한 환경에서는 Spring Boot의 Batch JDBC 초기화 설정을 별도로 지정해야 합니다.

컴파일 검증은 프로젝트 디렉터리에서 실행합니다.

```bash
./gradlew compileKotlin
```

## 실행 시 주의사항

- 이 모듈은 주제별 예제를 한곳에 모아 둔 학습 코드이며, `job` 패키지에 여러 `@SpringBootApplication`과 `main` 함수가 존재합니다.
- Job/Step Bean 이름도 일부 중복되므로 전체 패키지를 한 번에 스캔하면 Bean 충돌이 발생할 수 있습니다. 예제를 실행할 때는 대상 설정만 로딩하도록 패키지를 분리하거나 Profile/컴포넌트 스캔 범위를 적용해야 합니다.
- `HelloWorldJob`의 `fileName`은 필수이며 `.csv`로 끝나야 합니다. `name`은 선택 파라미터입니다.
- `MethodInvokingTaskletConfiguration`은 `message` Job 파라미터를 사용합니다.
- `ConditionalJob`은 학습을 위해 항상 예외를 발생시키도록 구성되어 있습니다.
- `SystemCommandJob`은 `/tmp.txt` 삭제 명령을 실행합니다. `AdvancedSystemCommandJob`은 `${user.home}/tem/spring-batch` 디렉터리를 만들고 그 안에 `tmp.txt`를 생성합니다. 시스템 명령 예제는 실행 전에 대상 경로와 명령을 반드시 확인해야 합니다.
