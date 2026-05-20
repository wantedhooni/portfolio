#!/bin/bash
set -e

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"

"$ROOT_DIR/script/all-stop.sh"
"$ROOT_DIR/script/all-start.sh"
