#!/bin/bash
set -e

cd "$(dirname "$0")/.."

# 1) PM2로 기동된 경우 정상 종료
if command -v pm2 >/dev/null 2>&1; then
  pm2 delete ecosystem.config.js || true
fi

# 2) 잔여 프로세스 포트 기준 강제 정리 (fallback)
kill -9 $(ss -lntp '( sport = :18081 )' | grep -oP 'pid=\K\d+') 2>/dev/null || true
kill -9 $(ss -lntp '( sport = :18091 )' | grep -oP 'pid=\K\d+') 2>/dev/null || true
