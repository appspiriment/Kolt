#!/usr/bin/env bash
# Symlinks this repo's steering set into an app project.
# Usage: scripts/link-standards.sh <kmp|android> <path-to-app-repo>
set -euo pipefail

type="$1"
target="$2"
standards_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

case "$type" in
  kmp|android) ;;
  *) echo "type must be 'kmp' or 'android'" >&2; exit 1 ;;
esac

ln -sfn "$standards_dir/steering/$type" "$target/.standards"
ln -sfn .standards/AGENTS.md "$target/AGENTS.md"
ln -sfn .standards/AGENTS.md "$target/CLAUDE.md"
ln -sfn .standards/AGENTS.md "$target/GEMINI.md"
