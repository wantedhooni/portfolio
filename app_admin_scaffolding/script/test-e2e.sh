#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
# shellcheck disable=SC1091
source "${SCRIPT_DIR}/common.sh"

printf '[info] admin e2e port=%s\n' "${ADMIN_E2E_PORT}"
printf '[info] service e2e port=%s\n' "${SERVICE_E2E_PORT}"

(
  cd "${ROOT_DIR}/frontend/ui-admin"
  E2E_BASE_URL="http://127.0.0.1:${ADMIN_E2E_PORT}" npm run test:e2e
)

(
  cd "${ROOT_DIR}/frontend/ui-web-service"
  E2E_BASE_URL="http://127.0.0.1:${SERVICE_E2E_PORT}" npm run test:e2e
)

printf '[done] all e2e tests passed\n'
