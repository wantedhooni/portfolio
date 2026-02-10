#!/bin/bash

cd "$(dirname "$0")/web-ui"

# 기존 프로세스 종료
pkill -f "vite.*5173" 2>/dev/null || true

# 환경 선택 (기본값: uat)
ENV=demo
echo "환경: $ENV"

# npm 의존성 설치 (필요한 경우)
if [ ! -d "node_modules" ]; then
  npm install
fi

# 백그라운드 실행
npm run dev > /tmp/web-ui.log 2>&1 &

echo "웹 UI 시작됨 (PID: $!)"
echo "로그: tail -f /tmp/web-ui.log"


