#!/bin/bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"

cd "$ROOT_DIR"

bash ./script/all-stop.sh
sleep 2
bash ./script/all-start.sh
