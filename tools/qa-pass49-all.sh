#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
python tools/qa-pass49-source.py
bash tools/qa-pass49-region-continuity.sh
bash tools/qa-pass47-all.sh
printf '\nPass49 and prior Pass41-48 offline QA passed. NeoForge build/client not verified.\n'
