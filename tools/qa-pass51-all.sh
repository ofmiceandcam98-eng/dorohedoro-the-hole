#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
python3 tools/qa-pass51-source.py
bash tools/qa-pass51-route.sh
bash tools/qa-pass50-all.sh
printf '\nPASS Pass51 and Pass41-50 offline regressions; NOT an installable NeoForge build.\n'
