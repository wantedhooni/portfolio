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

## Docker / ECR / ECS 배포
이 프로젝트는 정적 파일을 Nginx로 서빙하도록 구성되어 있어 ECS에 바로 배포할 수 있습니다.

### 로컬 Docker 빌드/실행
```bash
docker build --build-arg BUILD_MODE=uat -t trading-ui:local .
docker run --rm -p 8080:80 trading-ui:local
```

### 멀티 플랫폼 빌드 (buildx)
```bash
docker buildx build --platform linux/amd64,linux/arm64 \
  --build-arg BUILD_MODE=uat \
  -t trading-ui:latest \
  .
```

### ECR에 푸시 (예시)
```bash
# 1) ECR 로그인
aws ecr get-login-password --region ap-northeast-2 | docker login --username AWS --password-stdin <ACCOUNT_ID>.dkr.ecr.ap-northeast-2.amazonaws.com

# 2) 빌드 & 태그
docker buildx build --platform linux/amd64,linux/arm64 \
  --build-arg BUILD_MODE=uat \
  -t trading-ui:latest \
  .
docker tag trading-ui:latest <ACCOUNT_ID>.dkr.ecr.ap-northeast-2.amazonaws.com/trading-ui:latest

# 3) 푸시
docker push <ACCOUNT_ID>.dkr.ecr.ap-northeast-2.amazonaws.com/trading-ui:latest
```

### ECS 설정 팁
- 컨테이너 포트: `80`
- 헬스체크 경로: `/` (기본)
- 배포 프로파일은 `uat`입니다. Docker 빌드 시 `--build-arg BUILD_MODE=uat`로 고정합니다.
- `VITE_API_BASE`는 `.env.uat`에 정의된 값을 사용합니다.
