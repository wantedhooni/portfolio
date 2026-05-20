#!/bin/bash
set -e

./script/all-stop.sh
echo 'node kill'
./script/all-start.sh
