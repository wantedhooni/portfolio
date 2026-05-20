#!/bin/bash
set -e

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"

echo "[core-banking] backend infra start"
(
  cd "$ROOT_DIR/backend"
  ./script/infra-all-start.sh
)

echo "[core-banking] backend app start"
(
  cd "$ROOT_DIR/backend"
  ./script/app-all-start.sh
)

echo "[core-banking] frontend app start"
(
  cd "$ROOT_DIR/frontned"
  ./script/all-start.sh
)

cat <<'INFO'

[core-banking] 접속 정보
- api-admin Swagger: http://localhost:8081/swagger-ui/index.html
- api-saas Swagger:  http://localhost:8091/swagger-ui/index.html
- web-admin:         http://localhost:18081
- web-saas:          http://localhost:18091

[데모 계정]
- 관리자: admin@example.com / Qwer1234!
- 사용자: demo@example.com / Qwer1234!
INFO
