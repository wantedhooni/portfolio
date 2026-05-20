#!/bin/bash
set -e

./script/app-all-stop.sh || true
echo 'node kill'
./script/app-all-start.sh
