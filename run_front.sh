#!/bin/bash

# Demo 환경으로 실행
ENV=demo

# web-ui를 백그라운드로 실행
cd "$(dirname "$0")/web-ui"

echo "Demo 환경으로 실행합니다."
cp .env.uat .env.current

# 기존 프로세스 종료 (있으면)
pkill -f "vite.*5173" 2>/dev/null

# npm 의존성 설치 (필요한 경우)
if [ ! -d "node_modules" ]; then
  npm install
fi

# Vite dev 서버를 백그라운드로 실행 (demo 환경)
npm run dev > /tmp/web-ui.log 2>&1 &

# PID 저장
echo $! > /tmp/web-ui.pid

echo "웹 UI가 백그라운드로 실행 중입니다. (환경: demo)"
echo "PID: $(cat /tmp/web-ui.pid)"
echo "로그: tail -f /tmp/web-ui.log"
