#!/bin/bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"

cd "$ROOT_DIR"

echo "[1/3] WEB UI 종료"
pkill -f "vite --host 0.0.0.0 --port 5173" 2>/dev/null || true

echo "[2/3] API 서버 종료"
if lsof -ti tcp:8080 >/dev/null 2>&1; then
  lsof -ti tcp:8080 | xargs kill -9
fi

echo "[3/3] 인프라 종료"
docker compose -f ./docker/elk/docker-compose.yml down || true
docker compose -f ./docker/yfinance-server/docker-compose.yml down || true
docker compose -f ./docker-compose.yml down || true

echo "전체 서비스 종료 완료"
