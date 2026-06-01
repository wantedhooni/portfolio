# Spring Boot 4 + QueryDSL SQL `INSERT SELECT` Demo

Native SQL 문자열을 Repository에 직접 두지 않고, **QueryDSL SQL**로 다음 유형의 쿼리를 작성하는 데모 프로젝트다.

```sql
INSERT INTO daily_product_sales_stat (...)
SELECT ..., SUM(...), COUNT(...)
FROM orders
WHERE ...
GROUP BY ...;
```

## 스택

- Java 21
- Spring Boot 4.0.6
- Spring JDBC
- QueryDSL SQL 5.1.0
- H2 In-Memory DB

Spring Boot 4.0.6은 Spring 공식 프로젝트 페이지 기준 현재 4.0.x 라인으로 표시된다. QueryDSL SQL 5.1.0은 javadoc.io 기준 최신 `querydsl-sql` 문서 버전이다.

## 핵심 포인트

- `JPAQueryFactory`가 아니라 `SQLQueryFactory` 사용
- `RelationalPathBase` 기반 테이블 Q타입 직접 정의
- `queryFactory.insert(stat).columns(...).select(...).execute()` 형태로 `INSERT SELECT` 작성
- 30만 건 데모 데이터는 `src/main/resources/data.sql`에서 H2 `SYSTEM_RANGE(1, 300000)`로 생성

## 실행

```bash
./gradlew bootRun
```

로컬에 Gradle이 없다면 Gradle Wrapper를 추가해서 실행한다.

```bash
gradle wrapper --gradle-version 9.0
./gradlew bootRun
```

> 이 압축파일에는 wrapper jar를 포함하지 않았다. 저장소에 wrapper binary를 둘지 여부는 팀 정책에 맞춰 결정하면 된다.

## API

### 1. 주문 데이터 건수 확인

```bash
curl http://localhost:8080/api/demo/orders/count
```

예상 결과:

```json
{"orderCount":300000}
```

### 2. 일별 상품 통계 재생성

```bash
curl -X POST 'http://localhost:8080/api/demo/stats/daily?date=2026-05-01'
```

내부적으로 다음 흐름을 QueryDSL SQL로 실행한다.

1. `daily_product_sales_stat`에서 해당 날짜 통계 삭제
2. `orders`를 `product_id` 기준으로 집계
3. `daily_product_sales_stat`에 `INSERT SELECT`

### 3. 통계 row 수 확인

```bash
curl 'http://localhost:8080/api/demo/stats/daily/count?date=2026-05-01'
```

### 4. 통계 샘플 조회

```bash
curl 'http://localhost:8080/api/demo/stats/daily?date=2026-05-01&limit=10'
```

## 주요 코드

```java
return queryFactory
        .insert(stat)
        .columns(
                stat.statDate,
                stat.productId,
                stat.totalAmount,
                stat.orderCount
        )
        .select(
                SQLExpressions
                        .select(
                                Expressions.constant(targetDate),
                                orders.productId,
                                orders.amount.sum(),
                                orders.id.count()
                        )
                        .from(orders)
                        .where(
                                orders.orderedAt.goe(from),
                                orders.orderedAt.lt(to)
                        )
                        .groupBy(orders.productId)
        )
        .execute();
```

## 파일 구성

```text
src/main/java/com/example/querydslsqldemo
├── QuerydslSqlDemoApplication.java
├── config/QuerydslSqlConfig.java
├── querydsl/QOrders.java
├── querydsl/QDailyProductSalesStat.java
├── repository/DailyProductSalesStatSqlRepository.java
├── service/DailyProductSalesStatService.java
└── web/DemoController.java

src/main/resources
├── application.yml
├── schema.sql
└── data.sql
```

## 주의점

- QueryDSL SQL은 JPA 영속성 컨텍스트를 사용하지 않는다.
- JPA 엔티티 Q타입과 QueryDSL SQL Q타입은 다르다.
- 운영 DB에서는 H2 `SYSTEM_RANGE` 대신 별도 fixture 또는 적재 스크립트를 사용해야 한다.
- DB 함수까지 완전한 컴파일 타임 검증이 필요한 통계/정산 시스템이라면 jOOQ도 검토 대상이다.
