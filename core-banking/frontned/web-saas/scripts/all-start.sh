#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
LOG_FILE="${ROOT_DIR}/.next-dev.log"
PID_FILE="${ROOT_DIR}/.next-dev.pid"
SCREEN_NAME="web-saas-dev"
PORT="${PORT:-18091}"
BASE_API="${NEXT_PUBLIC_BASE_API_URL:-http://localhost:8091}"
DEMO_USER="${NEXT_PUBLIC_DEMO_USER:-demo@corebanking.local}"
DEMO_PASSWORD="${NEXT_PUBLIC_DEMO_USER_PASSWORD:-demo1234!}"

cd "${ROOT_DIR}"

if [[ -f "${PID_FILE}" ]] && grep -q "^screen:" "${PID_FILE}"; then
  echo "web-saas 개발 서버가 이미 screen 세션으로 실행 중입니다. ${SCREEN_NAME}"
elif [[ -f "${PID_FILE}" ]] && kill -0 "$(cat "${PID_FILE}")" 2>/dev/null; then
  echo "web-saas 개발 서버가 이미 실행 중입니다. PID=$(cat "${PID_FILE}")"
elif command -v screen >/dev/null 2>&1; then
  echo "web-saas 개발 서버를 screen 세션으로 시작합니다..."
  screen -S "${SCREEN_NAME}" -X quit >/dev/null 2>&1 || true
  screen -dmS "${SCREEN_NAME}" bash -lc "
    cd '${ROOT_DIR}' &&
    NEXT_PUBLIC_BASE_API_URL='${BASE_API}' \
    NEXT_PUBLIC_DEMO_USER='${DEMO_USER}' \
    NEXT_PUBLIC_DEMO_USER_PASSWORD='${DEMO_PASSWORD}' \
    npm run dev -- --hostname 127.0.0.1 > '${LOG_FILE}' 2>&1
  "
  echo "screen:${SCREEN_NAME}" > "${PID_FILE}"
else
  echo "web-saas 개발 서버를 시작합니다..."
  nohup env \
    NEXT_PUBLIC_BASE_API_URL="${BASE_API}" \
    NEXT_PUBLIC_DEMO_USER="${DEMO_USER}" \
    NEXT_PUBLIC_DEMO_USER_PASSWORD="${DEMO_PASSWORD}" \
    npm run dev -- --hostname 127.0.0.1 > "${LOG_FILE}" 2>&1 &
  echo "$!" > "${PID_FILE}"
fi

echo ""
echo "접속 URL: http://localhost:${PORT}"
echo "API URL: ${BASE_API}"
echo "데모 계정: ${DEMO_USER}"
echo "데모 비밀번호: ${DEMO_PASSWORD}"
echo "로그 파일: ${LOG_FILE}"
