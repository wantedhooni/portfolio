#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
# shellcheck disable=SC1091
source "${SCRIPT_DIR}/common.sh"

stop_process "ui-admin"
stop_process "ui-web-service"
stop_process "api-service-server"
stop_process "api-admin-server"

(
  cd "${ROOT_DIR}"
  docker compose --env-file ./.env -f ./infra/db/docker-compose.yml down -v
)

printf '[done] all services stopped\n'
