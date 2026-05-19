#!/bin/bash
set -e

here=$(pwd)
#redis
cd ./infra/redis
docker compose up -d

cd "$here"
# pg
cd ./infra/pgpool
docker compose up -d

cd "$here"
# metrics
cd ./infra/metrics
docker compose up -d