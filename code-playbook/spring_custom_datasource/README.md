# spring_custom_datasource

프로파일별 DataSource 구성과 읽기/쓰기 라우팅을 실험하는 샘플입니다.

## 구현 범위

- Thymeleaf 기반 게시글 CRUD 화면
- H2 단일 DB 프로파일
- MariaDB 단일 DB 프로파일
- MariaDB primary / secondary 라우팅 프로파일
- read-only 트랜잭션일 때 secondary 사용

## 프로파일

- `h2`: H2 메모리 DB
- `mariadb`: MariaDB primary 단일 사용
- `mariadb_repl`: primary / secondary 라우팅

## 접속 정보

- 화면: `http://localhost:8080/posts`
- H2 Console: `http://localhost:8080/h2-console`
- MariaDB primary: `localhost:23306`
- MariaDB secondary: `localhost:33306`

## 라우팅 방식

- `RoutingDataSource`가 `TransactionSynchronizationManager.isCurrentTransactionReadOnly()`를 기준으로 분기합니다.
- 쓰기 트랜잭션은 primary
- 읽기 전용 트랜잭션은 secondary

## 메모

- 애플리케이션 레벨 라우팅 예제를 구현해둔 상태입니다.
- 운영 환경에서는 ProxySQL, MaxScale 같은 프록시 계층 검토가 더 현실적이라는 메모도 남겨두고 있습니다.
