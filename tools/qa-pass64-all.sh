#!/bin/bash
set -euo pipefail
cd "$(dirname "$0")/.."
bash tools/qa-pass64-buffer.sh
python3 tools/qa-pass64-source.py
bash tools/qa-pass63-all.sh
printf '\nPASS Pass64 including Pass63/62/61 offline QA. Full NeoForge and Minecraft testing still pending.\n'
