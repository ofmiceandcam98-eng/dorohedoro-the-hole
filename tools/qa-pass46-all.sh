#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
python tools/qa-pass46-source.py
bash tools/qa-pass46-skyline.sh
bash tools/qa-pass45-all.sh
printf '\nPass46 offline source and Java logic checks passed. No actual NeoForge build or client render was performed.\n'
