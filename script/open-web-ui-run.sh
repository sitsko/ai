#!/bin/bash

RED='\033[0;31m'
NC='\033[0m'
cd ..
source .env
echo "*******************************************************************"
echo "Starting Open Web UI"
echo "*******************************************************************"

curl -LsSf https://astral.sh/uv/install.sh | sh
DATA_DIR=~/.open-webui uvx --python 3.11 open-webui@latest serve