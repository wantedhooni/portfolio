# spring_excel_down

게시글 데이터를 Excel로 내려받는 방식을 비교하는 샘플입니다.

## 구현 범위

- 게시글 CRUD API
- Apache POI `SXSSFWorkbook` 기반 Excel 다운로드
- 대용량 다운로드 방식 2가지 비교

## 주요 API

- `POST /api/posts/create`
- `GET /api/posts/{id}`
- `GET /api/posts`
- `PUT /api/posts/{id}`
- `DELETE /api/posts/{id}`
- `GET /api/posts/excel/large-download/v1`
- `GET /api/posts/excel/large-download/v2`

## 다운로드 방식

- `v1`: 한 번에 큰 조회를 수행한 뒤 Excel 작성
- `v2`: 페이지 단위로 끊어서 조회하면서 Excel 작성

## 실행 정보

- 실행: `./gradlew bootRun`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- API Docs: `http://localhost:8080/api-docs`
- H2 Console: `http://localhost:8080/h2-console`
- H2 JDBC: `jdbc:h2:tcp://localhost:13306/mem:testdb;DB_CLOSE_ON_EXIT=FALSE`

## 메모

- `SXSSFWorkbook` 특성상 Excel 행 제한은 `1,048,576`입니다.
- 이 한계를 넘는 데이터는 CSV 비동기 생성 방식이 더 적합해서 `make_bulk_csv` 모듈에서 따로 다룹니다.
