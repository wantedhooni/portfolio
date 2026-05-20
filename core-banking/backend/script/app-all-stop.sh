#!/bin/bash
set -e

kill $(lsof -ti :8081)
kill $(lsof -ti :8091)