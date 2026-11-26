#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
python tools/qa-pass45-source.py
bash tools/qa-pass45-courts.sh
bash tools/qa-pass44-all.sh
printf '\nPass45 offline QA passed. NO Minecraft client/server or complete NeoForge build was run.\n'
