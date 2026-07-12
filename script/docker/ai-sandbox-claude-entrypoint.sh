#!/bin/bash
set -e

# ~/.claude.json lives directly under $HOME (not inside ~/.claude/), but only
# ~/.claude/ is a persisted volume. Keep the real file inside the volume and
# symlink it into place so it survives container recreation.
[ -e "$HOME/.claude.json" ] || ln -s "$HOME/.claude/.claude.json" "$HOME/.claude.json"

exec "$@"
