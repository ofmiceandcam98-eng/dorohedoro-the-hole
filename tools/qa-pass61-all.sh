#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
python3 tools/qa-pass61-source.py
bash tools/qa-pass61-sewers.sh
bash tools/qa-pass60-all.sh
printf 'PASS Pass61 and Pass41-60 offline chain (not actual NeoForge compilation).\n'
