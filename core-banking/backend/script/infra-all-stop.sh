#!/bin/bash
set -e

docker compose -f ./infra/redis/docker-compose.yml down -v
#pggool
docker compose -f ./infra/pgpool/docker-compose.yml down -v
# kafka
docker compose -f ./infra/kafka/docker-compose.yml down -v
# metrics()
docker compose -f ./infra/metrics/docker-compose.yml down -v
#ELK
docker compose -f ./infra/elk/docker-compose.yml down -v