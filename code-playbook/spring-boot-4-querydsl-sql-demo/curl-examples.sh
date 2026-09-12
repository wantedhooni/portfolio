#!/usr/bin/env bash
set -euo pipefail

curl http://localhost:8080/api/demo/orders/count
printf '\n'

curl -X POST 'http://localhost:8080/api/demo/stats/daily?date=2026-05-01'
printf '\n'

curl 'http://localhost:8080/api/demo/stats/daily/count?date=2026-05-01'
printf '\n'

curl 'http://localhost:8080/api/demo/stats/daily?date=2026-05-01&limit=10'
printf '\n'
