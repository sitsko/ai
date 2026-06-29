#!/usr/bin/env bash
#
# PreToolUse hook — blocks any Read, Bash, or Grep tool call that targets .env* files.
# Receives a JSON payload on stdin from Claude Code.

set -euo pipefail

DENY_RESPONSE='{
  "hookSpecificOutput": {
    "hookEventName": "PreToolUse",
    "permissionDecision": "deny",
    "permissionDecisionReason": "Access to .env* file is denied"
  }
}'

input=$(cat)
tool=$(echo "$input" | jq -r '.tool_name')

deny() {
  echo "$DENY_RESPONSE"
  exit 0
}

# Matches: .env, .env.local, env.private, env.aws, path/to/env.secret
# Does NOT match: environment.txt, myenv.txt
env_pattern() { grep -iqE '\.env|(^|[^a-zA-Z0-9])env\.'; }

case "$tool" in
  Read|Edit)
    target=$(echo "$input" | jq -r '.tool_input.file_path // ""')
    if echo "$target" | env_pattern; then deny; fi
    ;;
  Bash)
    target=$(echo "$input" | jq -r '.tool_input.command // ""')
    if echo "$target" | env_pattern; then deny; fi
    ;;
  Grep)
    path=$(echo "$input"    | jq -r '.tool_input.path // ""')
    include=$(echo "$input" | jq -r '.tool_input.include // ""')
    if echo "$path"    | env_pattern; then deny; fi
    if echo "$include" | env_pattern; then deny; fi
    ;;
esac
