#!/bin/bash

RED='\033[0;31m'
NC='\033[0m'
cd ..
source .env
echo "*******************************************************************"
echo "Starting app in container at local machine using podman in detach mode:"

podman compose --env-file .env -f src/main/docker/docker-compose.yaml up --build --detach \
  && echo "*******************************************************************" \
  && echo "Current status of all containers" \
  && echo "*******************************************************************" \
  && podman ps --all