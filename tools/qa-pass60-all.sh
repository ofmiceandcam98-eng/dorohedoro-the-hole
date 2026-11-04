#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
python3 tools/qa-pass60-source.py
bash tools/qa-pass60-freight.sh
bash tools/qa-pass59-all.sh
printf '\nPASS Pass60 and Pass41–59 offline checks. Full Minecraft/NeoForge runtime NOT verified.\n'
