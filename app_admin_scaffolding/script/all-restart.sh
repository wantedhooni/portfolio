#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROFILE="${1:-}"

"${SCRIPT_DIR}/all-stop.sh"
"${SCRIPT_DIR}/all-start.sh" "${PROFILE}"
