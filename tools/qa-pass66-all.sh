#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
bash tools/qa-pass66-coasts.sh
python3 tools/qa-pass66-source.py
bash tools/qa-pass65-all.sh
printf '\nPASS Pass66 coast and historical offline suites. Full NeoForge build remains unverified.\n'
