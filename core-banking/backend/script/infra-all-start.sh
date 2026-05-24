#!/bin/bash
set -e

#redis
docker compose -f ./infra/redis/docker-compose.yml  up -d
#pggool
docker compose -f ./infra/pgpool/docker-compose.yml up -d
# kafka
docker compose -f ./infra/kafka/docker-compose.yml up -d

# metrics()
docker compose -f ./infra/metrics/docker-compose.yml up -d
#ELK
docker compose -f ./infra/elk/docker-compose.yml up -d
