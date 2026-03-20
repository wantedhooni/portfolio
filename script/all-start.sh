#!/bin/bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"

cd "$ROOT_DIR"

echo "[1/3] 인프라 기동"
bash ./run_infra.sh

echo "[2/3] API 서버 기동"
if lsof -ti tcp:8080 >/dev/null 2>&1; then
  lsof -ti tcp:8080 | xargs kill -9
fi
nohup ./gradlew api-server:bootRun > /tmp/securities-api.log 2>&1 &

echo "[3/3] WEB UI 기동"
if pgrep -f "vite --host 0.0.0.0 --port 5173" >/dev/null 2>&1; then
  pkill -f "vite --host 0.0.0.0 --port 5173"
fi
(cd web-ui && nohup npm run dev > /tmp/securities-web-ui.log 2>&1 &)

sleep 3

echo ""
echo "접속 정보"
echo "- UI: http://localhost:5173"
echo "- API: http://localhost:8080"
echo "- Swagger: http://localhost:8080/swagger-ui.html"
echo "- Demo Account: user1@example.com"
echo "- Demo Password: Password!"
echo "- API 로그: tail -f /tmp/securities-api.log"
echo "- UI 로그: tail -f /tmp/securities-web-ui.log"
