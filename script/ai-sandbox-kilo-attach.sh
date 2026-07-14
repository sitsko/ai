#!/bin/bash

RED='\033[0;31m'
NC='\033[0m'

echo "*******************************************************************"
echo "Attach Sandbox"
echo "*******************************************************************"

docker compose -f docker/ai-sandbox-kilo-docker-compose.yml exec ai-sandbox-kilo bash