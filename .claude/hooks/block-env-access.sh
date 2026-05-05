#!/usr/bin/env bash
#
# PreToolUse hook — blocks any Read, Bash, or Grep tool call that targets .env* files.
# Receives a JSON payload on stdin from Claude Code.

set -euo pipefail

DENY_RESPONSE='{
  "hookSpecificOutput": {
    "hookEventName": "PreToolUse",
    "permissionDecision": "deny",
    "permissionDecisionReason": "Access to .env file is denied"
  }
}'

input=$(cat)
tool=$(echo "$input" | jq -r '.tool_name')

deny() {
  echo "$DENY_RESPONSE"
  exit 0
}

case "$tool" in
  Read)
    target=$(echo "$input" | jq -r '.tool_input.file_path // ""')
    if echo "$target" | grep -qE '\.env'; then deny; fi
    ;;
  Bash)
    target=$(echo "$input" | jq -r '.tool_input.command // ""')
    if echo "$target" | grep -qE '\.env'; then deny; fi
    ;;
  Grep)
    target=$(echo "$input" | jq -r '.tool_input.path // ""')
    if echo "$target" | grep -qE '\.env'; then deny; fi
    ;;
esac
