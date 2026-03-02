# spring_jpa_user

사용자 도메인을 JPA로 미리 설계해보는 모델링 샘플입니다.

## 현재 구현

- `User`
- `UserDetail`
- `Role`
- `Account`
- `User` ↔ `Role` 다대다 매핑
- `User` ↔ `UserDetail` 일대일 매핑
- 시작 시 샘플 사용자 데이터 생성

## 현재 상태

- REST API보다는 엔티티 설계와 관계 매핑 확인이 목적입니다.
- OpenAPI 설정은 있지만 실제 컨트롤러는 아직 없습니다.

## 실행 정보

- 실행: `./gradlew bootRun`
- H2 Console: `http://localhost:8080/h2-console`
- H2 JDBC: `jdbc:h2:tcp://localhost:13306/mem:testdb`

## 메모

- `InitRunner`가 샘플 사용자 데이터를 넣습니다.
- `@ManyToMany`, `@OneToOne` 설계 감을 유지하려는 용도의 작은 저장소입니다.
