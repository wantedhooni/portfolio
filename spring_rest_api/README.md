# spring_rest_api

게시글 CRUD를 가장 단순한 형태로 정리한 REST API 샘플입니다.

## 구현 범위

- 게시글 생성
- 게시글 단건 조회
- 게시글 페이징 조회
- 게시글 수정
- 게시글 삭제
- 전역 예외 처리

## 주요 API

- `POST /api/posts/create`
- `GET /api/posts/{id}`
- `GET /api/posts?page=0&size=10`
- `PUT /api/posts/{id}`
- `DELETE /api/posts/{id}`

## 실행 정보

- 실행: `./gradlew bootRun`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- API Docs: `http://localhost:8080/api-docs`
- H2 Console: `http://localhost:8080/h2-console`
- H2 JDBC: `jdbc:h2:tcp://localhost:13306/mem:testdb`

## 메모

- `PostApi`, `PostService`, `PostRepository`로 계층을 분리한 기본형입니다.
- 대용량 다운로드나 메시징 같은 확장 주제는 다른 모듈에서 실험합니다.
