#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
ENV_FILE="${ROOT_DIR}/.env"
LOG_DIR="${ROOT_DIR}/logs"
PID_DIR="${LOG_DIR}/pids"

mkdir -p "${LOG_DIR}" "${PID_DIR}"

if [[ -f "${ENV_FILE}" ]]; then
  set -a
  # shellcheck disable=SC1090
  source "${ENV_FILE}"
  set +a
fi

DEFAULT_PROFILE="${APP_PROFILE:-${SPRING_PROFILES_ACTIVE:-local}}"
DB_PORT="${DB_PORT:-23306}"
ADMIN_API_PORT="${ADMIN_API_PORT:-8080}"
SERVICE_API_PORT="${SERVICE_API_PORT:-9090}"
ADMIN_FRONTEND_PORT="${ADMIN_FRONTEND_PORT:-3000}"
SERVICE_FRONTEND_PORT="${SERVICE_FRONTEND_PORT:-3001}"
ADMIN_E2E_PORT="${ADMIN_E2E_PORT:-3100}"
SERVICE_E2E_PORT="${SERVICE_E2E_PORT:-3101}"

resolve_profile() {
  local requested_profile="${1:-}"

  if [[ -n "${requested_profile}" ]]; then
    printf '%s\n' "${requested_profile}"
    return
  fi

  printf '%s\n' "${DEFAULT_PROFILE}"
}

pid_file_for() {
  local service_name="${1}"
  printf '%s/%s.pid\n' "${PID_DIR}" "${service_name}"
}

is_running() {
  local pid="${1}"
  kill -0 "${pid}" >/dev/null 2>&1
}

start_process() {
  local service_name="${1}"
  local log_file="${2}"
  shift 2

  local pid_file
  pid_file="$(pid_file_for "${service_name}")"

  if [[ -f "${pid_file}" ]]; then
    local existing_pid
    existing_pid="$(cat "${pid_file}")"
    if [[ -n "${existing_pid}" ]] && is_running "${existing_pid}"; then
      printf '[skip] %s already running (pid=%s)\n' "${service_name}" "${existing_pid}"
      return
    fi
    rm -f "${pid_file}"
  fi

  nohup "$@" >"${log_file}" 2>&1 &
  local pid=$!
  printf '%s\n' "${pid}" > "${pid_file}"
  printf '[start] %s (pid=%s) -> %s\n' "${service_name}" "${pid}" "${log_file}"
}

stop_process() {
  local service_name="${1}"
  local pid_file
  pid_file="$(pid_file_for "${service_name}")"

  if [[ ! -f "${pid_file}" ]]; then
    printf '[skip] %s pid file not found\n' "${service_name}"
    return
  fi

  local pid
  pid="$(cat "${pid_file}")"

  if [[ -z "${pid}" ]]; then
    rm -f "${pid_file}"
    printf '[skip] %s pid file empty\n' "${service_name}"
    return
  fi

  if ! is_running "${pid}"; then
    rm -f "${pid_file}"
    printf '[skip] %s already stopped (pid=%s)\n' "${service_name}" "${pid}"
    return
  fi

  kill "${pid}" >/dev/null 2>&1 || true

  for _ in {1..10}; do
    if ! is_running "${pid}"; then
      break
    fi
    sleep 1
  done

  if is_running "${pid}"; then
    kill -9 "${pid}" >/dev/null 2>&1 || true
  fi

  rm -f "${pid_file}"
  printf '[stop] %s (pid=%s)\n' "${service_name}" "${pid}"
}
