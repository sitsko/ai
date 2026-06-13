#!/bin/bash

RED='\033[0;31m'
NC='\033[0m'
cd ..
source .env
echo "*******************************************************************"
echo "Download Nemotron Nano 30B from hugging face (Warning, the GGUF size is ~ 38 G) "
echo "*******************************************************************"

#curl http://localhost:30000/v1/chat/completions \
#  -H "Content-Type: application/json" \
#  -d '{
#    "model": "nemotron",
#    "messages": [{"role": "user", "content": "New York is a great city because..."}],
#    "max_tokens": 100
#  }'

http POST http://localhost:30000/v1/chat/completions \
  Content-Type:application/json \
  model=nemotron \
  messages:='[{"role": "user", "content": "New York is a great city because..."}]' \
  max_tokens:=100