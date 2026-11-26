#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
python tools/qa-pass43-source.py
bash tools/qa-pass43-placement.sh
python tools/qa-pass42-source.py
bash tools/qa-pass42-region.sh
python tools/qa-pass41-static.py
python tools/qa-pass39-interiors.py
python tools/qa-pass35-inhabited-night-static.py
