#!/bin/bash
set -e

# frontned 루트로 이동 (스크립트 위치 기준)
cd "$(dirname "$0")/.."

# 1) PM2 확보 (없으면 설치)
if ! command -v pm2 >/dev/null 2>&1; then
  echo "[frontend] pm2 미설치 → 전역 설치"
  npm install -g pm2
fi

# 2) 빌드 (각 앱)
echo "[frontend] web-admin build"
( cd web-admin && cp -f .env.dev .env && npm install && npm run build )

echo "[frontend] web-saas build"
( cd web-saas && cp -f .env.dev .env && npm install && npm run build )

# 3) PM2로 기동 (이미 떠 있으면 무중단 reload)
echo "[frontend] pm2 start (auto-restart + memory limit + logs)"
pm2 startOrReload ecosystem.config.js
pm2 save           # 프로세스 목록 저장 (서버 재부팅 시 pm2 resurrect 로 복구)
pm2 status

cat <<'INFO'

[frontend] PM2 운영 명령
- 상태:        pm2 status
- 로그(실시간): pm2 logs web-admin   (또는 web-saas)
- 로그 파일:   ~/.pm2/logs/web-admin-out.log / web-admin-error.log
- 재시작:      pm2 restart web-admin
- 중지/삭제:   pm2 delete web-admin
- 부팅 자동기동: pm2 startup  (출력되는 명령 1회 실행) → 이후 pm2 save

[행 재발 시] pm2 logs 또는 error 로그에서 "JavaScript heap out of memory" 확인
INFO
