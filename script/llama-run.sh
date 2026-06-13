#!/bin/bash

RED='\033[0;31m'
NC='\033[0m'
cd ..
source .env
echo "*******************************************************************"
echo "Starting llama.cpp with Nemotron locally"

llama-server \
  --model ~/models/nemotron3-gguf/Nemotron-3-Nano-30B-A3B-UD-Q8_K_XL.gguf \
  --host 0.0.0.0 \
  --port 30000 \
  --n-gpu-layers 99 \
  --ctx-size 8192 \
  --threads 8
