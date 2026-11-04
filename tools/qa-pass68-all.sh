#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
bash tools/qa-pass68-pump-halls.sh
python3 tools/qa-pass68-source.py
bash tools/qa-pass67-all.sh
printf '\nPASS Pass68 pump galleries and historical offline regression coverage; NOT a NeoForge build or actual Minecraft test.\n'
