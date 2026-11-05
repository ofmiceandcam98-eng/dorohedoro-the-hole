#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
python3 tools/qa-pass59-source.py
bash tools/qa-pass59-courts.sh
bash tools/qa-pass58-all.sh
printf '\nPASS Pass59 and Pass41–58 offline checks; NOT a NeoForge runtime build.\n'
