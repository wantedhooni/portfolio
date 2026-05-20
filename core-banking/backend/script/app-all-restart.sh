#!/bin/bash
set -e

./script/all-stop.sh || true
echo 'node kill'
./script/all-start.sh
