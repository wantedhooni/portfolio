#!/bin/bash
set -e

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"

stop_port() {
  local port="$1"
  local pids
  pids="$(lsof -tiTCP:"$port" -sTCP:LISTEN 2>/dev/null || true)"
  if [ -n "$pids" ]; then
    echo "[core-banking] stop port $port: $pids"
    kill $pids 2>/dev/null || true
  fi
}

echo "[core-banking] app stop"
stop_port 18081
stop_port 18091
stop_port 8081
stop_port 8091

echo "[core-banking] backend infra stop"
(
  cd "$ROOT_DIR/backend"
  ./script/infra-all-stop.sh || true
)

echo "[core-banking] stopped"
