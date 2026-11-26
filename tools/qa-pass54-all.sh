#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
python3 tools/qa-pass54-source.py
bash tools/qa-pass54-lanes.sh
bash tools/qa-pass53-all.sh
printf '\nPASS: Pass54 geometry and Pass41-53 offline regression chain.\n'
printf 'No NeoForge compile, runData, client, dedicated server or GameTests run here.\n'
