# make_bulk_csv

1억건 이상 대용량 데이터를 만들어 보자

엑셀 기준 SXSSFWorkbook limit = 1_048_576이니
루프 돌면서 계속 쓰면 되겠지만 스트림 다운로드는 불가능 할거 같다.

1억건 이상이면 머~~ 만들면서 다운로드는 무리수이니
백그라운드에서 CSV를 만들어 주고 다운받아라가 맞을거 같다.

작업 목록
---
- [x] JDBC 쿼리 결과를 바로 CSV로 쓰는 방식
- - JPA의 querydsl을 native query로 변경 방식 사용
- - String 쿼리 지옥은 싫으니
- [x] Spring batch의 ItemReader -> Chunk ->ItemWriter 방식 쓰기 개발
- [x] local에 CSV 쓰기
- [x] S3에 CSV 쓰기

```
SXSSFWorkbook limit = 1_048_576
POI 기반 SXSSFWorkbook row limit
Invalid row number (1048576) outside allowable range (0..1048575)
1048576보다 크면 csv가 맞는거 같다.
```

### 잡담
```

이거 보니 세일즈포스 대용량 CSV 다운로드가 생각난다.

세일즈 포스 대용량 CSV 다운로드 플로우
1. 대용량 다운로드 요청
2. 작업이 완료되면 Email이 올거라고 알려줌
3. 작업이 완료 되면 Email에 다운로드 링크 전달.



실무에서 사용할려면
1. 대용량 다운로드 요청
2. 작업이 완료되면 email or 메신저로 알려줄거라고 알림 (비동기 요청)
- Queue
- Quartz
- Async 쓰레드 사용
3. 백그라운드 executor가 작업
- 로컬 저장소
- S3
4. 원래라면 파일 다운로드 시스템에 메타 데이터 기록하고, 이를 기반으로 다운로드링크 만들어준다.
- 사용자가 어떤 데이터를 만들어 달라고 요청했는지, 다운로드 언제 받았는지 기록해야 한다.
5. 사용자에게 noti
- 요청자만 우효하게
- 다운로드 시점, 어떤 파일을 받았는지 기록
```

참 나는 이쁘게 꾸미는거는 정말 안되는거 같다.