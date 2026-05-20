#!/bin/bash
set -e

kill -9 $(ss -lntp '( sport = :18081 )' | grep -oP 'pid=\K\d+')
kill -9 $(ss -lntp '( sport = :18091 )' | grep -oP 'pid=\K\d+')

