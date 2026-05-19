#!/bin/bash
set -e

here=$(pwd)
#redis
cd ./infra/redis
docker compose down -v

cd "$here"
# pg
cd ./infra/pgpool
docker compose down -v

cd "$here"
# pg
cd ./infra/metrics
docker compose down -v
