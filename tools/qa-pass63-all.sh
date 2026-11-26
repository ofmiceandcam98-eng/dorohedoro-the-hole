#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
bash tools/qa-pass63-buffer.sh
python3 tools/qa-pass63-source.py
bash tools/qa-pass62-all.sh
printf '\nPASS Pass63 + Pass62 + Pass61 offline source/data checks. This is NOT a NeoForge build.\n'
