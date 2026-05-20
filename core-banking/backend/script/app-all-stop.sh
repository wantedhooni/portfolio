#!/bin/bash
set -e

kill -9 $(ss -lntp '( sport = :18081 )' | grep -oP 'pid=\K\d+') || true
kill -9 $(ss -lntp '( sport = :18091 )' | grep -oP 'pid=\K\d+') || true