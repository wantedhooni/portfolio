#!/bin/bash
set -e

echo "Waiting for primary MariaDB..."

until mariadb \
  -hmariadb-primary \
  -uroot \
  -p"${MARIADB_PRIMARY_ROOT_PASSWORD}" \
  -e "SELECT 1" >/dev/null 2>&1; do
  sleep 2
done

echo "Configuring MariaDB replication..."

mariadb -uroot -p"${MARIADB_ROOT_PASSWORD}" <<SQL
STOP SLAVE;
RESET SLAVE ALL;

CHANGE MASTER TO
  MASTER_HOST='mariadb-primary',
  MASTER_PORT=3306,
  MASTER_USER='repl',
  MASTER_PASSWORD='replpass',
  MASTER_USE_GTID=slave_pos;

START SLAVE;
SQL

echo "Replication started."