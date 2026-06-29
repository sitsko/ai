#!/bin/bash

RED='\033[0;31m'
NC='\033[0m'
cd ..
source .env
echo "*******************************************************************"
echo "Starting Langfuse"
echo "*******************************************************************"

docker compose -f script/docker/langfuse-docker-compose.yml up --detach