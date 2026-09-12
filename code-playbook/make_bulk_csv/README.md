# make_bulk_csv

대용량 데이터를 CSV로 비동기 생성하는 샘플입니다.

## 구현 범위

- `/exports` 요청을 받아 비동기 CSV 작업 시작
- Quartz가 Spring Batch 잡 실행
- Tasklet 방식과 Chunk 방식 둘 다 준비
- JDBC 스트리밍으로 ResultSet을 바로 CSV로 기록
- Querydsl SQL 기반 export query 생성
- 로컬 파일 출력
- S3 멀티파트 업로드 출력
- 컨트롤러 / 쿼리 생성기 테스트 포함

## 주요 API

- `POST /exports`
- 기본값은 tasklet 방식
- `tasklet=false`로 chunk 잡 선택

## 실행 흐름

1. 클라이언트가 `/exports` 호출
2. 컨트롤러가 `jobUuid`를 만들고 Quartz trigger 등록
3. Quartz가 Batch job 실행
4. `CsvExportService`가 JDBC 스트리밍으로 CSV 생성
5. 프로파일에 따라 로컬 또는 S3로 저장

## 설정 포인트

- DB: `jdbc:mariadb://localhost:23306/bulk_sample`
- fetch size: `bulk.export.fetch-size`
- 파일명 prefix: `bulk.export.output-filename-prefix`
- 출력 프로파일
  - `local`: 로컬 파일 시스템 저장
  - `s3`: S3 멀티파트 업로드

## 메모

- Excel 행 제한을 넘는 대량 다운로드는 CSV 백그라운드 생성 방식이 더 현실적이라는 전제로 만든 샘플입니다.
- `ExportControllerTest`, `UserExportQueryProviderTest`가 포함되어 있습니다.
