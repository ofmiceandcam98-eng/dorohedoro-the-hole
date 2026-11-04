#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
python3 tools/qa-pass53-source.py
bash tools/qa-pass53-parcels.sh
bash tools/qa-pass52-all.sh
printf '\nPASS Pass53 geometry fixture and prior Pass41-52 source/offline suites.\n'
printf 'NEOFORGE CLIENT/SERVER/COMPILATION REMAIN UNVERIFIED.\n'
