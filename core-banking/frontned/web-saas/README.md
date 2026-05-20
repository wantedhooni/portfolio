# web-saas

사용자 SaaS용 코어뱅킹 프론트엔드입니다. `saas-api-docs.json`의 SAAS-API 명세를 기준으로 인증, 계좌, 입출금, 주식 거래, 배당, 보유종목, 포트폴리오, 거래내역, 종목 조회 화면을 제공합니다.

## 기술 스택

- Next.js 16 App Router
- React 19
- TypeScript
- Tailwind CSS v4
- shadcn/ui
- axios

## 실행

```bash
./scripts/all-start.sh
```

접속 URL은 `http://localhost:18091`입니다.

중지와 재시작은 아래 스크립트를 사용합니다.

```bash
./scripts/all-stop.sh
./scripts/all-restart.sh
```

## 환경 변수

| 이름 | 기본값 | 설명 |
| --- | --- | --- |
| `NEXT_PUBLIC_BASE_API_URL` | `http://localhost:8091` | SAAS-API 서버 주소 |
| `NEXT_PUBLIC_DEMO_USER` | `demo@corebanking.local` | 화면 표시용 데모 계정 |
| `NEXT_PUBLIC_DEMO_USER_PASSWORD` | `demo1234!` | 화면 표시용 데모 비밀번호 |

## 주요 화면

- 로그인: 데모 모드와 실제 API 로그인 모드 전환
- 가입: 사용자 기본 정보 입력, 유효성 검증, 로컬 가입 요청 접수
- 대시보드: 총 자산, 현금, 평가금액, 손익률 요약
- 계좌: 계좌 검색, 선택, 잔고 확인
- 입출금: `/api/v1/accounts/{accountId}/deposit`, `/withdraw` 대응 폼
- 거래: `/trades/buy`, `/trades/sell`, `/trades/dividend` 대응 폼
- 종목: `/api/v1/stocks` 검색 결과 테이블
- 거래내역: `/api/v1/accounts/{accountId}/transactions` 결과 테이블

## API 연동 구조

API 호출은 `src/services/saasBankingService.ts`의 `SaasBankingService`가 담당합니다. 기존 `src/lib/api.ts` axios 인스턴스를 사용하므로 access token 자동 첨부와 refresh token 재발급 흐름을 그대로 재사용합니다.

백엔드가 실행되지 않는 개발 환경에서도 UI 확인이 가능하도록 데모 모드를 제공합니다. 실제 API 검증은 로그인 화면에서 `API` 모드를 선택하고 SAAS-API 서버를 `NEXT_PUBLIC_BASE_API_URL`에 맞춰 실행한 뒤 진행합니다.

현재 `saas-api-docs.json`에는 회원가입 엔드포인트가 없습니다. `/signup` 화면은 가입 요청을 입력받아 로컬 접수증으로 저장하며, 백엔드가 `/api/v1/auth/signup` 확장 API를 제공하면 같은 폼에서 API 제출 모드로 전환할 수 있습니다.

## 검증

```bash
npm run lint
npm run build
```
