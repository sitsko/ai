#!/bin/bash

RED='\033[0;31m'
NC='\033[0m'
cd ..
source .env
echo "*******************************************************************"
echo "Starting llama.cpp with Nemotron locally"
echo "*******************************************************************"

# it is too heavy model
#llama-server \
#  --model ~/models/nemotron3-gguf/Nemotron-3-Nano-30B-A3B-UD-Q8_K_XL.gguf \
#  --host 0.0.0.0 \
#  --port 30000 \
#  --n-gpu-layers 99 \
#  --ctx-size 8192 \
#  --threads 8

# run with downloading
#llama-server \
#  -hf nvidia/NVIDIA-Nemotron-3-Nano-4B-GGUF:Q4_K_M \
#  -c 0 \
#  --alias Nemotron-3-Nano-4B \
#  -ngl 999 \
#  --port 30000 \
#  --host 0.0.0.0


llama-server \
  --model ~/models/nemotron3-gguf/NVIDIA-Nemotron3-Nano-4B-Q4_K_M.gguf \
  --host 0.0.0.0 \
  --alias Nemotron-3-Nano-4B \
  --port 30000 \
  --n-gpu-layers 99 \
  --ctx-size 8192 \
  --threads 8
