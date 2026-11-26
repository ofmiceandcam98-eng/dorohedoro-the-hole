#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
bash tools/qa-pass70-sewer-architecture.sh
python3 tools/qa-pass70-source.py
bash tools/qa-pass69-all.sh
printf '\nPASS Pass70 new sewer architecture and historical offline regression chain. NOT a compiled NeoForge mod.\n'
