#!/bin/bash

cd "$(dirname "$0")/web-ui"

# 기존 프로세스 종료
pkill -f "vite.*5173" 2>/dev/null || true

# npm 의존성 설치 (필요한 경우)
if [ ! -d "node_modules" ]; then
  npm install
fi

# 환경 변수 설정
ENV_FILE=.env.demo;
echo "환경: $ENV_FILE"

# 백그라운드 실행
set -a; source "$ENV_FILE"; set +a; npm run dev > /tmp/web-ui.log 2>&1 &

echo "웹 UI 시작됨 (PID: $!)"
echo "로그: tail -f /tmp/web-ui.log"


