#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
bash tools/qa-pass67-service-bays.sh
python3 tools/qa-pass67-source.py
bash tools/qa-pass66-all.sh
printf '\nPASS Pass67 tunnel side bays and all historical offline QA; NeoForge and Minecraft not yet run.\n'
