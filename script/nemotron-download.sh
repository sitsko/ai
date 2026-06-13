#!/bin/bash

RED='\033[0;31m'
NC='\033[0m'
cd ..
source .env.private
echo "*******************************************************************"
echo "Download Nemotron Nano 30B from hugging face (Warning, the GGUF size is ~ 38 G) "
echo "*******************************************************************"

#hf download unsloth/Nemotron-3-Nano-30B-A3B-GGUF \
#  Nemotron-3-Nano-30B-A3B-UD-Q8_K_XL.gguf \
#  --local-dir ~/models/nemotron3-gguf

echo -e "$HF_TOKEN"

  hf download nvidia/NVIDIA-Nemotron-3-Nano-4B-GGUF \
   NVIDIA-Nemotron3-Nano-4B-Q4_K_M.gguf \
  --local-dir ~/models/nemotron3-gguf
