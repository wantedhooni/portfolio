#!/bin/bash
set -e

kill $(lsof -ti :18081)
kill $(lsof -ti :18091)

