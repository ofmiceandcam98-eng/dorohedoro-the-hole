#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
python3 tools/qa-pass57-source.py
bash tools/qa-pass57-belts.sh
bash tools/qa-pass56-all.sh
printf '\nPASS Pass57 + Pass41-56 offline regressions. Full NeoForge and Minecraft checks not run.\n'
