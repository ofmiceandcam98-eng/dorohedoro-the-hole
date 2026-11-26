#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
python3 tools/qa-pass62-vanilla-progression.py
python3 tools/qa-pass61-source.py
bash tools/qa-pass61-sewers.sh
printf '\nPASS Pass 62 source/data and previous Pass 61 sewer checks (NOT a NeoForge build).\n'
