#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
python3 tools/qa-pass55-source.py
bash tools/qa-pass55-wings.sh
bash tools/qa-pass54-all.sh
printf '\nPASS Pass55 geometry and Pass41-54 offline regression chain.\n'
printf 'No complete NeoForge compilation, client, server or live Minecraft visual QA performed.\n'
