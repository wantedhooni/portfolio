#!/bin/bash
set -e

PGDATA="${PGDATA:-/var/lib/postgresql/data}"
PRIMARY_HOST="${POSTGRES_PRIMARY_HOST:-pg_primary}"
PRIMARY_PORT="${POSTGRES_PRIMARY_PORT:-5432}"
REPL_USER="${POSTGRES_REPLICATION_USER:-replicator}"
REPL_PASS="${POSTGRES_REPLICATION_PASSWORD:-replicator_secret}"
PG_CONF="/etc/postgresql/postgresql.conf"

echo "[secondary] script started"
echo "[secondary] current user:"
id

mkdir -p "$PGDATA"
chown -R postgres:postgres "$PGDATA"
chmod 0700 "$PGDATA"

# 이미 복제 초기화된 경우 바로 기동
if [ -f "$PGDATA/standby.signal" ]; then
  echo "[secondary] standby.signal exists. Starting PostgreSQL via docker-entrypoint.sh"

  exec docker-entrypoint.sh postgres \
    -D "$PGDATA" \
    -c config_file="$PG_CONF"
fi

echo "[secondary] waiting for primary..."

until PGPASSWORD="$REPL_PASS" pg_isready \
  -h "$PRIMARY_HOST" \
  -p "$PRIMARY_PORT" \
  -U "$REPL_USER"; do
  sleep 2
done

echo "[secondary] primary is ready"
echo "[secondary] cleaning PGDATA..."

rm -rf "${PGDATA:?}"/*
chown -R postgres:postgres "$PGDATA"
chmod 0700 "$PGDATA"

echo "[secondary] running pg_basebackup..."

PGPASSWORD="$REPL_PASS" gosu postgres pg_basebackup \
  -h "$PRIMARY_HOST" \
  -p "$PRIMARY_PORT" \
  -U "$REPL_USER" \
  -D "$PGDATA" \
  -Fp \
  -Xs \
  -P \
  -R

echo "[secondary] pg_basebackup completed"

chown -R postgres:postgres "$PGDATA"
chmod 0700 "$PGDATA"

echo "[secondary] starting PostgreSQL via docker-entrypoint.sh"

exec docker-entrypoint.sh postgres \
  -D "$PGDATA" \
  -c config_file="$PG_CONF"