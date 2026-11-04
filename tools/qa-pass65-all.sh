#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
bash tools/qa-pass65-oceans.sh
bash tools/qa-pass65-bathymetry.sh
python3 tools/qa-pass65-source.py
bash tools/qa-pass64-all.sh
bash tools/qa-pass60-all.sh
printf '\nPASS Pass65 ocean policy and earlier offline regression chain. NeoForge actual build unverified.\n'
