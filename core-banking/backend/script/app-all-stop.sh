#!/bin/bash
set -e

kill -9 $(ss -lntp '( sport = :8091 )' | grep -oP 'pid=\K\d+')
kill -9 $(ss -lntp '( sport = :8091 )' | grep -oP 'pid=\K\d+')