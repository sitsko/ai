#!/bin/bash

RED='\033[0;31m'
NC='\033[0m'
cd ..

echo "*******************************************************************"
echo "Test Nemotron model (REST request)"
echo "*******************************************************************"

http POST http://localhost:30000/v1/chat/completions \
  Content-Type:application/json \
  model=nemotron \
  messages:='[{"role": "user", "content": "In which country placed Gdansk?"}]' \
  max_tokens:=100 \
  | jq .