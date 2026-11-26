#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
python tools/qa-pass47-source.py
python tools/qa-pass47-geometry.py
bash tools/qa-pass47-origin.sh
bash tools/qa-pass46-all.sh
printf '\nPASS Pass47 and earlier Pass41-46 offline suites. No Minecraft/NeoForge client/server build performed.\n'
