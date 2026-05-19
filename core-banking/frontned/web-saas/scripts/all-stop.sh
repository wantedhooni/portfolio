#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PID_FILE="${ROOT_DIR}/.next-dev.pid"
SCREEN_NAME="web-saas-dev"

if [[ -f "${PID_FILE}" ]] && grep -q "^screen:" "${PID_FILE}"; then
  echo "web-saas screen 세션을 중지합니다. ${SCREEN_NAME}"
  screen -S "${SCREEN_NAME}" -X quit >/dev/null 2>&1 || true
  PORT_PID="$(lsof -ti :18091 2>/dev/null | head -n 1 || true)"
  if [[ -n "${PORT_PID}" ]]; then
    kill "${PORT_PID}"
  fi
  rm -f "${PID_FILE}"
elif [[ -f "${PID_FILE}" ]] && kill -0 "$(cat "${PID_FILE}")" 2>/dev/null; then
  echo "web-saas 개발 서버를 중지합니다. PID=$(cat "${PID_FILE}")"
  kill "$(cat "${PID_FILE}")"
  rm -f "${PID_FILE}"
else
  screen -S "${SCREEN_NAME}" -X quit >/dev/null 2>&1 || true
  PORT_PID="$(lsof -ti :18091 2>/dev/null | head -n 1 || true)"
  if [[ -n "${PORT_PID}" ]]; then
    echo "PID 파일은 없지만 18091 포트 서버를 중지합니다. PID=${PORT_PID}"
    kill "${PORT_PID}"
  else
    echo "실행 중인 web-saas 개발 서버 PID 파일이 없습니다."
  fi
  rm -f "${PID_FILE}"
fi
