#!/bin/bash
set -e

cd web-admin && npm install && npm run start &
cd web-saas && npm install && npm run start &