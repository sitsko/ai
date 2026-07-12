#!/bin/bash

RED='\033[0;31m'
NC='\033[0m'

echo "*******************************************************************"
echo "Attach Sandbox"
echo "*******************************************************************"

docker compose -f docker/sandbox-docker-compose.yml exec ai-sandbox bash