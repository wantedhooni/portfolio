#!/bin/bash
set -e

./gradlew :application:api-admin:bootRun &
./gradlew :application:api-saas:bootRun &
./gradlew :application:api-executor:bootRun &