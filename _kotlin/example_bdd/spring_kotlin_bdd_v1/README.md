## 1. 프로젝트 README

> 이 섹션은 프로젝트 루트의 `README.md`로 사용할 수 있는 요약 문서입니다.
> 앞선 본문이 구현 상세와 코드 예제를 설명한다면, README는 프로젝트 목적, 실행 방법, 테스트 방법, 구조 파악에 필요한 정보를 중심으로 정리합니다.

### 1.1 프로젝트 개요

이 프로젝트는 **Spring Boot 4.x**, **Spring Data JPA**, **Cucumber BDD**, **Testcontainers**를 사용해 주문 도메인을 구현하고 검증하는 예제입니다.

단순 CRUD 중심의 예제가 아니라, 주문 생성·확정·취소 과정에서 발생하는 상태 전이와 재고 변경 규칙을 도메인 모델에 캡슐화하고, 이를 BDD 시나리오로 검증하는 데 초점을 둡니다.

### 1.2 핵심 목표

| 목표           | 설명                                                |
| ------------ | ------------------------------------------------- |
| 도메인 중심 설계    | 주문 상태 변경, 재고 차감, 재고 복구 규칙을 엔티티 내부 메서드로 표현         |
| 계층 분리        | Domain, Repository, Service, BDD Test 계층을 명확히 분리  |
| 동시성 고려       | 상품 재고 차감 시 비관적 락을 사용해 동시 주문 문제 완화                 |
| BDD 테스트      | Gherkin Feature와 Step Definition으로 비즈니스 행위 검증     |
| 실제 DB 기반 테스트 | Testcontainers로 PostgreSQL 컨테이너를 실행해 통합 테스트 환경 구성 |

### 1.3 기술 스택

| 영역               | 기술                         |
| ---------------- | -------------------------- |
| Language         | Kotlin                     |
| Framework        | Spring Boot 4.x            |
| Persistence      | Spring Data JPA, Hibernate |
| Database         | PostgreSQL                 |
| Test Framework   | JUnit 5                    |
| BDD              | Cucumber 7                 |
| Integration Test | Testcontainers             |
| Build Tool       | Gradle Kotlin DSL          |

### 1.4 주요 기능

#### 주문 생성

* 고객 ID와 상품 목록을 기반으로 주문을 생성합니다.
* 주문 수량은 1개 이상이어야 합니다.
* 상품 재고가 부족하면 주문 생성을 실패시킵니다.
* 주문 생성 시 상품 재고를 차감합니다.

#### 주문 확정

* `PENDING` 상태의 주문만 확정할 수 있습니다.
* 이미 확정된 주문을 다시 확정하려고 하면 예외가 발생합니다.
* 주문 확정 후 상태는 `CONFIRMED`가 됩니다.

#### 주문 취소

* 배송 완료된 주문은 취소할 수 없습니다.
* 주문 취소 시 기존 주문 수량만큼 상품 재고를 복구합니다.
* 주문 취소 후 상태는 `CANCELLED`가 됩니다.

#### 주문 조회

* 주문, 주문 상품, 상품 정보를 함께 조회합니다.
* `JOIN FETCH`를 사용해 주문 상세 조회 시 발생할 수 있는 N+1 문제를 줄입니다.

### 1.5 프로젝트 구조

```text
src
├── main/kotlin/com/example/shop
│   ├── domain
│   │   ├── order
│   │   │   ├── Order.kt
│   │   │   ├── OrderItem.kt
│   │   │   └── OrderStatus.kt
│   │   └── product
│   │       └── Product.kt
│   ├── repository
│   │   ├── OrderRepository.kt
│   │   └── ProductRepository.kt
│   └── service
│       └── OrderService.kt
└── test/kotlin/com/example/shop
    ├── bdd
    │   ├── CucumberTestSuite.kt
    │   ├── config
    │   │   ├── CucumberSpringConfig.kt
    │   │   └── TestcontainersConfig.kt
    │   └── steps
    │       └── OrderSteps.kt
    └── resources
        ├── application-test.yaml
        └── features
            └── order.feature
```

### 1.6 도메인 규칙

| 규칙       | 설명                                         |
| -------- | ------------------------------------------ |
| 주문 수량 검증 | 주문 수량은 1개 이상이어야 합니다.                       |
| 재고 검증    | 상품 재고가 주문 수량보다 적으면 주문할 수 없습니다.             |
| 재고 차감    | 주문 생성 시 상품 재고를 차감합니다.                      |
| 주문 확정    | `PENDING` 상태의 주문만 `CONFIRMED`로 변경할 수 있습니다. |
| 중복 확정 방지 | 이미 확정된 주문은 다시 확정할 수 없습니다.                  |
| 주문 취소    | 배송 완료 상태가 아닌 주문은 취소할 수 있습니다.               |
| 재고 복구    | 주문 취소 시 주문 수량만큼 상품 재고를 복구합니다.              |

### 1.7 사전 요구사항

로컬에서 프로젝트를 실행하려면 다음 환경이 필요합니다.

| 항목     | 설명                                  |
| ------ | ----------------------------------- |
| JDK    | Spring Boot 4.x 실행이 가능한 Java 버전     |
| Docker | Testcontainers 기반 PostgreSQL 실행에 필요 |
| Gradle | Gradle Wrapper 사용 시 별도 설치 불필요       |

> Testcontainers를 사용하므로 테스트 실행 전 Docker가 실행 중이어야 합니다.

### 1.8 실행 방법

#### 전체 테스트 실행

```bash
./gradlew test
```

#### Cucumber BDD 테스트만 실행

```bash
./gradlew test --tests "*.CucumberTestSuite"
```

#### 특정 태그 테스트 실행

```bash
./gradlew test -Dcucumber.filter.tags="@smoke"
```

### 1.9 BDD 시나리오

BDD 시나리오는 다음 파일에서 관리합니다.

```text
src/test/resources/features/order.feature
```

대표 시나리오는 다음과 같습니다.

| 시나리오          | 검증 내용                            |
| ------------- | -------------------------------- |
| 정상 주문 생성      | 주문 상태, 주문 총액, 상품 재고 차감 검증        |
| 재고 부족 시 주문 실패 | 재고 부족 예외 메시지 검증                  |
| 주문 확정         | `PENDING` → `CONFIRMED` 상태 변경 검증 |
| 중복 확정 방지      | 확정된 주문을 다시 확정할 때 예외 발생 검증        |
| 주문 취소         | 주문 상태 변경 및 상품 재고 복구 검증           |

### 1.10 테스트 리포트

Cucumber 테스트 실행 후 다음 경로에 리포트가 생성됩니다.

```text
build/cucumber-reports/
├── cucumber.json
└── cucumber.html
```

| 파일              | 용도                     |
| --------------- | ---------------------- |
| `cucumber.json` | CI 도구 연동용 리포트          |
| `cucumber.html` | 브라우저에서 확인 가능한 HTML 리포트 |

### 1.11 테스트 환경

테스트는 실제 PostgreSQL을 직접 설치하지 않고, Testcontainers를 통해 PostgreSQL 컨테이너를 실행합니다.

```kotlin
PostgreSQLContainer("postgres:16-alpine")
    .withDatabaseName("shop_test")
    .withReuse(true)
```

Spring Boot의 `@ServiceConnection`을 사용하면 컨테이너의 DataSource 설정을 테스트 컨텍스트에 자동으로 연결할 수 있습니다.

### 1.12 설계 포인트

#### 도메인 로직 캡슐화

주문 상태 변경과 재고 변경 규칙은 서비스 계층에 흩어두지 않고 `Order`, `Product` 엔티티 내부 메서드로 캡슐화합니다.

```kotlin
order.addItem(product, quantity)
order.confirm()
order.cancel()
```

#### 트랜잭션 경계 분리

조회 메서드는 `@Transactional(readOnly = true)`를 기본으로 사용하고, 상태 변경이 필요한 유스케이스에만 별도의 `@Transactional`을 적용합니다.

#### 동시성 제어

상품 재고 차감은 동시에 여러 요청이 들어올 수 있으므로 상품 조회 시 비관적 락을 사용합니다.

```kotlin
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT p FROM Product p WHERE p.id = :id")
fun findByIdWithLock(@Param("id") id: Long): Product?
```

### 1.13 확장 아이디어

* 주문 API Controller 추가
* 결제 상태 모델링
* 배송 상태 전이 규칙 추가
* 낙관적 락 기반 재고 차감 방식 비교
* Cucumber 태그 기반 CI 파이프라인 분리
* 테스트 데이터 초기화 전략 개선
* REST Docs 또는 OpenAPI 기반 API 문서화 추가

### 1.14 운영 적용 시 보완 사항

이 프로젝트는 주문 도메인의 핵심 비즈니스 규칙을 BDD 방식으로 검증하는 샘플입니다. 실제 운영 서비스에 적용할 경우 다음 항목을 별도로 설계해야 합니다.

| 항목    | 설명                              |
| ----- | ------------------------------- |
| 인증·인가 | 고객별 주문 접근 제어 필요                 |
| 결제    | 결제 승인, 실패, 취소, 환불 상태 모델링 필요     |
| 배송    | 배송 준비, 출고, 배송 완료 상태 전이 필요       |
| 장애 처리 | 외부 결제·배송 시스템 연동 실패 대응 필요        |
| 감사 로그 | 주문 상태 변경 이력 저장 필요               |
| 모니터링  | 주문 실패율, 재고 부족, DB 락 대기 시간 관측 필요 |
