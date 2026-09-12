
#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
# shellcheck disable=SC1091
source "${SCRIPT_DIR}/common.sh"

(
  cd "${ROOT_DIR}/frontend/ui-web-service"
  npm install
  export PORT="${SERVICE_FRONTEND_PORT}"
  npm run dev
)
