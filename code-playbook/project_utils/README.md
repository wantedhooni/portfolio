# project_utils

프로젝트에서 자주 재사용할 만한 작은 유틸 코드를 모아두는 모듈입니다.

## 현재 포함된 유틸

- jqGrid filter DTO
- RSQL 파서
- RSQL expression 모델
- Querydsl predicate builder
- 공통 예외 모델

## 용도

- 검색 조건 문자열을 파싱해서 Querydsl 조건으로 바꾸는 실험
- jqGrid 스타일 필터 요청을 Java 객체로 다루는 유틸

## 현재 상태

- 라이브러리성 코드가 중심이며, 별도 실행용 애플리케이션은 아닙니다.
- 필요할 때 다른 샘플 프로젝트에서 가져다 쓰기 위한 성격이 강합니다.
