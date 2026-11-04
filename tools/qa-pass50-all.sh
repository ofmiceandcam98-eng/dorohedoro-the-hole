#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
python tools/qa-pass50-source.py
bash tools/qa-pass50-safety.sh
bash tools/qa-pass49-all.sh
printf '\nPASS Pass50 and legacy offline regressions; NO NeoForge build or in-game world verification.\n'
