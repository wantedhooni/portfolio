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