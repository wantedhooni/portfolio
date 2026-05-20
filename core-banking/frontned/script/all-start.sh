#!/bin/bash
set -e

cd web-admin && npm install && npm run dev &
cd web-saas && npm install && npm run dev &