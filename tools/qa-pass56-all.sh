#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
python3 tools/qa-pass56-source.py
bash tools/qa-pass56-yards.sh
bash tools/qa-pass55-all.sh
printf '\nPASS Pass56 covered industrial service passages + Pass41-55 offline regression chain.\n'
printf 'Complete NeoForge compilation, game client, dedicated server, and live Minecraft visual QA not yet run.\n'
