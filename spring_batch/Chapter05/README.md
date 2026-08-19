# Spring Batch Chapter 05

《The Definitive Guide to Spring Batch》(한국어판: 《스프링 배치 완벽 가이드 2판》)의 Spring Batch JobRepository 탐색 예제를 Kotlin과 Spring Batch 6 API로 정리한 학습용 프로젝트입니다.

## 학습 내용

- `JobBuilder`와 `StepBuilder`를 이용한 Tasklet 기반 Job 구성
- `JobRepository`를 통한 Job Instance 조회
- Job Instance별 Job Execution 이력 조회
- Spring Batch 메타데이터와 Job 실행 이력의 관계 확인

## 기술 스택

| 구분 | 버전/구성 |
| --- | --- |
| Kotlin | 2.2.21 |
| Java | 21 |
| Spring Boot | 4.0.7 |
| Spring Batch | Spring Boot 관리 버전(Spring Batch 6 계열) |
| Gradle | 9.6.0 Wrapper |
| Database | PostgreSQL |

Spring Batch 메타데이터는 JDBC 기반 `JobRepository`에 저장됩니다. JPA와 Querydsl 의존성도 포함되어 있지만, 현재 Chapter 05 예제에는 Entity나 Querydsl 조회 코드가 없습니다.

## 프로젝트 구조

```text
src/main
├── kotlin/com/revy/batch/chapter05
│   ├── DemoApplication.kt
│   ├── ExploringTasklet.kt
│   └── batch
│       └── BatchChapter05.kt
└── resources
    └── application.yml
```

| 파일 | 역할 |
| --- | --- |
| `DemoApplication.kt` | Spring Boot 애플리케이션 진입점 |
| `BatchChapter05.kt` | `explorerJob`, `explorerStep`, `explorerTasklet` Bean 구성 |
| `ExploringTasklet.kt` | 현재 Job의 Instance와 Execution 이력 조회 및 로깅 |
| `application.yml` | PostgreSQL, JPA, Spring Batch 실행 설정 |

## Job 구성

| 구분 | 이름 | 설명 |
| --- | --- | --- |
| Job | `explorerJob` | JobRepository 실행 이력을 탐색하는 단일 Step Job |
| Step | `explorerStep` | `ExploringTasklet`을 한 번 실행하는 Tasklet Step |
| Tasklet | `explorerTasklet` | Job Instance 목록과 Instance별 Job Execution 목록 조회 |

```text
explorerJob
└── explorerStep
    └── ExploringTasklet
        ├── StepContext에서 현재 Job 이름 확인
        ├── JobRepository.findJobInstances(jobName)
        └── JobRepository.getJobExecutions(jobInstance)
```

`ExploringTasklet`은 `chunkContext.stepContext.jobName`으로 현재 Job 이름을 얻은 뒤, 같은 이름의 모든 Job Instance를 조회합니다. 각 Instance에 속한 Job Execution 목록을 로그로 출력하고 `RepeatStatus.FINISHED`를 반환합니다.

## 실행 환경

로컬 PostgreSQL에 다음 접속 정보가 준비되어 있어야 합니다.

```yaml
url: jdbc:postgresql://localhost:5432/app
username: app
password: app
```

Spring Batch 메타데이터 테이블이 없다면 `application.yml`의 `spring.batch.jdbc.initialize-schema` 설정을 환경에 맞게 활성화하거나 스키마를 미리 생성해야 합니다. 현재 해당 설정은 주석 처리되어 있습니다.

## 실행 및 검증

프로젝트 디렉터리에서 다음 명령을 실행합니다.

```bash
./gradlew bootRun
```

Spring Batch는 기본적으로 애플리케이션 시작 시 `explorerJob`을 실행합니다. 같은 Job Parameter로 완료된 Job Instance는 다시 실행할 수 없으므로, 반복 실행 시에는 새 Job Parameter를 전달합니다.

```bash
./gradlew bootRun --args='run.id=1'
```

컴파일만 검증하려면 다음 명령을 사용합니다.

```bash
./gradlew compileKotlin
```

## 현재 구현 주의사항

- `ExploringTasklet`의 로그 메시지에 있는 `%d`는 포맷 인자와 연결되지 않아 문자 그대로 출력됩니다.
- 현재 로그는 Instance/Execution의 개별 ID나 Exit Status 필드가 아니라 객체 전체를 문자열로 출력합니다.
- `spring.jpa.hibernate.ddl-auto` 값은 `create-drop`이지만 Spring Batch JDBC 메타데이터 테이블 생성을 대체하지 않습니다.
