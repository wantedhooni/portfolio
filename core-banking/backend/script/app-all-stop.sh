#!/bin/bash
set -e

kill -9 $(ss -lntp '( sport = :8081 )' | grep -oP 'pid=\K\d+') || true
kill -9 $(ss -lntp '( sport = :8091 )' | grep -oP 'pid=\K\d+') || true
kill -9 $(ss -lntp '( sport = :8071 )' | grep -oP 'pid=\K\d+') || true