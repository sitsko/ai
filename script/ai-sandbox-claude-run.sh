#!/bin/bash

RED='\033[0;31m'
NC='\033[0m'
cd ..

echo "*******************************************************************"
echo "Starting Sandbox"
echo "*******************************************************************"

export PROJECT_DIR=~/projects/ai
docker compose -f script/docker/ai-sandbox-claude-docker-compose.yml up -d