#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
# shellcheck disable=SC1091
source "${SCRIPT_DIR}/common.sh"

PROFILE="$(resolve_profile "${1:-}")"

printf '[info] profile=%s\n' "${PROFILE}"

(
  cd "${ROOT_DIR}"
  docker compose --env-file ./.env -f ./infra/db/docker-compose.yml up -d
)

start_process \
  "api-admin-server" \
  "${LOG_DIR}/api-admin-server.log" \
  bash -lc "cd '${ROOT_DIR}/backand' && export SPRING_PROFILES_ACTIVE='${PROFILE}' && exec ./gradlew :application:api-admin-server:bootRun"

start_process \
  "api-service-server" \
  "${LOG_DIR}/api-service-server.log" \
  bash -lc "cd '${ROOT_DIR}/backand' && export SPRING_PROFILES_ACTIVE='${PROFILE}' && exec ./gradlew :application:api-service-server:bootRun"

start_process \
  "ui-admin" \
  "${LOG_DIR}/ui-admin.log" \
  bash -lc "cd '${ROOT_DIR}/frontend/ui-admin' && export APP_PROFILE='${PROFILE}' PORT='${ADMIN_FRONTEND_PORT}' NEXT_PUBLIC_DISABLE_DEVTOOLS='true' NEXT_PUBLIC_ENABLE_DEVTOOLS='false' NODE_OPTIONS='--max_old_space_size=4096' && if [[ ! -d node_modules ]]; then npm install; fi && exec npx refine dev --devtools=false"

start_process \
  "ui-web-service" \
  "${LOG_DIR}/ui-web-service.log" \
  bash -lc "cd '${ROOT_DIR}/frontend/ui-web-service' && export APP_PROFILE='${PROFILE}' PORT='${SERVICE_FRONTEND_PORT}' && if [[ ! -d node_modules ]]; then npm install; fi && exec npm run dev"

printf '[done] all services started\n'
printf '\n'
printf 'Infra\n'
printf '  MariaDB: localhost:%s\n' "${DB_PORT}"
printf '  Database: app\n'
printf '  Username: app\n'
printf '  Password: app\n'
printf '\n'
printf 'Services\n'
printf '  Admin Frontend: http://localhost:%s\n' "${ADMIN_FRONTEND_PORT}"
printf '  Service Frontend: http://localhost:%s\n' "${SERVICE_FRONTEND_PORT}"
printf '  Admin API Swagger: http://localhost:%s/swagger-ui/index.html\n' "${ADMIN_API_PORT}"
printf '  Service API Swagger: http://localhost:%s/swagger-ui/index.html\n' "${SERVICE_API_PORT}"
printf '\n'
printf 'Logs\n'
printf '  ui-admin: %s\n' "${LOG_DIR}/ui-admin.log"
printf '  ui-web-service: %s\n' "${LOG_DIR}/ui-web-service.log"
printf '  api-admin-server: %s\n' "${LOG_DIR}/api-admin-server.log"
printf '  api-service-server: %s\n' "${LOG_DIR}/api-service-server.log"
