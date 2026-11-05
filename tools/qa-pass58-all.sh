#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
python3 tools/qa-pass58-source.py
bash tools/qa-pass58-annex.sh
bash tools/qa-pass57-all.sh
printf '\nPASS Pass58 + Pass41-57 offline regressions. Full NeoForge/Minecraft build NOT verified.\n'
