# Trading Platform UI

UI는 codex를 통한 바이브 코딩으로 진행
간단한 주식 차트 프론트엔드입니다.

## 환경 변수
- `VITE_API_BASE` : 백엔드 API 기본 URL (예: `http://localhost:8080`).
  - 개발 시 `.env` 또는 `.env.local`에 값을 설정하세요.
  - 예시 파일은 `.env.example`에 있습니다.

## 실행
1. 의존성 설치
```bash
npm install
```
2. 개발 서버 실행
```bash
npm run dev
```
3. 빌드
```bash
npm run build
```

## 구현한 주요 항목
- Vite + React 기반 UI
- `src/api/api.js` : 인증 및 시장 API 래퍼 (VITE_API_BASE 사용)
- Candlestick 차트 (`lightweight-charts`)와 기본 조회 UI

---
필요하면 배포용 Dockerfile, CI 설정, 더 자세한 환경변수 문서를 추가해드리겠습니다.