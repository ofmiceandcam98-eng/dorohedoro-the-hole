#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
bash tools/qa-pass69-sluice-valve.sh
python3 tools/qa-pass69-source.py
bash tools/qa-pass68-all.sh
printf '\nPASS Pass69 mechanical sluice layout and previous offline regression chain. NOT a NeoForge build or real redstone/fluid test.\n'
