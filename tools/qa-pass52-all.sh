#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
python3 tools/qa-pass52-source.py
bash tools/qa-pass52-climate-urban.sh
python3 tools/qa-pass48-build-preflight.py
bash tools/qa-pass51-all.sh
printf '\nPASS Pass52 and previous Pass41-51 offline regressions.\nNo full NeoForge build, game world or actual climate noise was tested.\n'
